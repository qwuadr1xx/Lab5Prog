package ru.qwuadrixx.balancer.di

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.RUDPBalancer
import ru.qwuadrixx.balancer.health.NodeHealthChecker
import ru.qwuadrixx.balancer.selector.ServerNode

private val log = LoggerFactory.getLogger("BalancerModule")

@Serializable
data class ServerAddress(val host: String, val port: Int)

@Serializable
data class BalancerConfig(
    val port: Int = 8080,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000,
    val pingTimeoutMs: Int = 1000,
    val healthPortOffset: Int = 1000,
    val healthCheckIntervalMs: Long = 5000,
    val servers: List<ServerAddress> = emptyList()
)

fun loadBalancerConfig(): BalancerConfig {
    val externalPath = System.getenv("BALANCER_CONFIG")
    val stream = externalPath
        ?.let { java.io.File(it).takeIf { f -> f.exists() }?.inputStream() }
        ?: object {}.javaClass.getResourceAsStream("/balancer.yml")
        ?: return BalancerConfig().also { log.warn("balancer.yml не найден, используются значения по умолчанию") }
    return try {
        Yaml.default.decodeFromString(BalancerConfig.serializer(), stream.bufferedReader().readText())
            .also { log.info("Конфигурация: port={}, серверов={}", it.port, it.servers.size) }
    } catch (e: Exception) {
        log.error("Ошибка загрузки balancer.yml: {}", e.message)
        BalancerConfig()
    }
}

val balancerModule = module {
    single { loadBalancerConfig() }
    single { get<BalancerConfig>().servers.map { ServerNode(it.host, it.port) } }
    single {
        val config = get<BalancerConfig>()
        NodeHealthChecker(
            nodes = get(),
            healthPortOffset = config.healthPortOffset,
            intervalMs = config.healthCheckIntervalMs,
            timeoutMs = config.socketTimeoutMs
        )
    }
    single {
        val config = get<BalancerConfig>()
        RUDPBalancer(config.port, get(), config.maxRetries, config.socketTimeoutMs, config.pingTimeoutMs)
    }
}