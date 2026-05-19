package ru.qwuadrixx.balancer.di

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.RUDPBalancer
import ru.qwuadrixx.balancer.auth.AdminTokenCache
import ru.qwuadrixx.balancer.auth.TokenCacheEvictor
import ru.qwuadrixx.balancer.commands.BalancerCommandHandler
import ru.qwuadrixx.balancer.health.NodeHealthChecker
import ru.qwuadrixx.balancer.manager.INodeManager
import ru.qwuadrixx.balancer.manager.NodeManager
import ru.qwuadrixx.balancer.process.IProcessManager
import ru.qwuadrixx.balancer.process.ProcessManager
import ru.qwuadrixx.balancer.registry.ServerRegistry
import ru.qwuadrixx.balancer.selector.ServerNode
import utils.TokenUtils

private val log = LoggerFactory.getLogger("BalancerModule")

@Serializable
data class ServerAddress(val host: String, val port: Int)

@Serializable
data class BalancerConfig(
    val port: Int = 8080,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000,
    val pingTimeoutMs: Int = 1000,
    val healthCheckIntervalMs: Long = 5000,
    val tokenSecret: String = "default-rudp-token-secret",
    val servers: List<ServerAddress> = emptyList()
)

fun loadBalancerConfig(): BalancerConfig {
    val externalPath = System.getenv("BALANCER_CONFIG")
    val stream = externalPath
        ?.let { java.io.File(it).takeIf { f -> f.exists() }?.inputStream() }
        ?: object {}.javaClass.getResourceAsStream("/balancer.yml")
        ?: return BalancerConfig().also { log.warn("balancer.yml не найден, используются значения по умолчанию") }
    val base = try {
        Yaml.default.decodeFromString(BalancerConfig.serializer(), stream.bufferedReader().readText())
    } catch (e: Exception) {
        log.error("Ошибка загрузки balancer.yml: {}", e.message)
        BalancerConfig()
    }
    val tokenSecret = System.getenv("TOKEN_SECRET") ?: base.tokenSecret
    return base.copy(tokenSecret = tokenSecret)
        .also { log.info("Конфигурация: port={}, статических серверов={}", it.port, it.servers.size) }
}

val balancerModule = module {
    single { loadBalancerConfig() }
    single {
        val cfg = get<BalancerConfig>()
        ServerRegistry(cfg.servers.map { ServerNode(it.host, it.port) })
    }
    single<INodeManager> { NodeManager(get()) }
    single<IProcessManager> { ProcessManager() }
    single { TokenUtils(get<BalancerConfig>().tokenSecret) }
    single { AdminTokenCache() }
    single { TokenCacheEvictor(get()) }
    single { BalancerCommandHandler(get(), get(), get()) }
    single { NodeHealthChecker(get()) }
    single { RUDPBalancer(get(), get(), get(), get()) }
}