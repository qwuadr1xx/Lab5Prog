package ru.qwuadrixx.balancer

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.health.NodeHealthChecker
import ru.qwuadrixx.balancer.selector.ServerNode

private val logger = LoggerFactory.getLogger("Main")

@Serializable
private data class ServerAddress(val host: String, val port: Int)

@Serializable
private data class BalancerConfig(
    val port: Int = 8080,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000,
    val pingTimeoutMs: Int = 1000,
    val healthPortOffset: Int = 1000,
    val healthCheckIntervalMs: Long = 5000,
    val servers: List<ServerAddress> = emptyList()
)

private fun loadConfig(): BalancerConfig {
    val externalPath = System.getenv("BALANCER_CONFIG")
    val stream = externalPath
        ?.let { java.io.File(it).takeIf { f -> f.exists() }?.inputStream() }
        ?: object {}.javaClass.getResourceAsStream("/balancer.yml")
        ?: return BalancerConfig().also { logger.warn("balancer.yml не найден, используются значения по умолчанию") }
    return try {
        Yaml.default.decodeFromString(BalancerConfig.serializer(), stream.bufferedReader().readText())
            .also { logger.info("Конфигурация: port={}, серверов={}", it.port, it.servers.size) }
    } catch (e: Exception) {
        logger.error("Ошибка загрузки balancer.yml: {}", e.message)
        BalancerConfig()
    }
}

fun main() {
    val config = loadConfig()
    if (config.servers.isEmpty()) {
        logger.error("Список серверов пуст — балансировщик не запущен")
        return
    }
    val nodes = config.servers.map { ServerNode(it.host, it.port) }

    val healthChecker = NodeHealthChecker(
        nodes = nodes,
        healthPortOffset = config.healthPortOffset,
        intervalMs = config.healthCheckIntervalMs,
        timeoutMs = config.socketTimeoutMs
    )
    Thread(healthChecker, "health-checker").also { it.isDaemon = true }.start()

    val balancer = RUDPBalancer(config.port, nodes, config.maxRetries, config.socketTimeoutMs, config.pingTimeoutMs)
    balancer.start()
}