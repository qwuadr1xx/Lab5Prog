@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.di

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import net.assembler.Assembler
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import ru.qwuadrixx.client.HealthCheckListener
import ru.qwuadrixx.client.RUDPServer
import ru.qwuadrixx.managers.CollectionManager
import ru.qwuadrixx.managers.RequestManager

private val log = LoggerFactory.getLogger("ServerModule")

@Serializable
data class ServerConfig(
    val port: Int = 8081,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000,
    val healthPortOffset: Int = 1000
)

fun loadServerConfig(): ServerConfig {
    val stream = object {}.javaClass.getResourceAsStream("/server.yml")
        ?: return ServerConfig().also { log.warn("server.yml не найден, используются значения по умолчанию") }
    val base = try {
        Yaml.default.decodeFromString(ServerConfig.serializer(), stream.bufferedReader().readText())
    } catch (e: Exception) {
        log.error("Ошибка загрузки server.yml: {}", e.message)
        ServerConfig()
    }
    val port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: base.port
    return base.copy(port = port)
        .also { log.info("Конфигурация: port={}, maxRetries={}, socketTimeoutMs={}", it.port, it.maxRetries, it.socketTimeoutMs) }
}

val serverModule = module {
    single { loadServerConfig() }
    single { CollectionManager() }
    single { RequestManager(get()) }
    single {
        val config = get<ServerConfig>()
        HealthCheckListener(config.port + config.healthPortOffset)
    }
    single {
        val config = get<ServerConfig>()
        RUDPServer(Assembler(), get(), config.port, config.maxRetries, config.socketTimeoutMs)
    }
}
