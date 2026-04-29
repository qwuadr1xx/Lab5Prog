package ru.qwuadrixx

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import net.assembler.Assembler
import org.slf4j.LoggerFactory
import ru.qwuadrixx.client.HealthCheckListener
import ru.qwuadrixx.client.RUDPServer
import ru.qwuadrixx.managers.CollectionManager
import ru.qwuadrixx.managers.FileManager
import ru.qwuadrixx.managers.FileWatcher
import ru.qwuadrixx.managers.RequestManager
import java.util.Vector
import kotlin.uuid.ExperimentalUuidApi

private val logger = LoggerFactory.getLogger("Main")

@Serializable
private data class ServerConfig(
    val port: Int = 8081,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000,
    val healthPortOffset: Int = 1000
)

private fun loadConfig(): ServerConfig {
    val stream = object {}.javaClass.getResourceAsStream("/server.yml")
        ?: return ServerConfig().also { logger.warn("server.yml не найден, используются значения по умолчанию") }
    val base = try {
        Yaml.default.decodeFromString(ServerConfig.serializer(), stream.bufferedReader().readText())
    } catch (e: Exception) {
        logger.error("Ошибка загрузки server.yml: {}", e.message)
        ServerConfig()
    }
    val port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: base.port
    return base.copy(port = port)
        .also { logger.info("Конфигурация: port={}, maxRetries={}, socketTimeoutMs={}", it.port, it.maxRetries, it.socketTimeoutMs) }
}

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val config = loadConfig()
    val saveFileName = System.getenv("SAVE_FILE_NAME") ?: "shared/save.csv"

    val fileManager = FileManager(saveFileName)
    val (initialVersion, loadedCollection) = fileManager.readCollection() ?: Pair("", emptyList())
    val collectionManager = CollectionManager(
        collection = Vector(loadedCollection),
        currentVersion = initialVersion
    )
    val requestManager = RequestManager(collectionManager, fileManager)

    val fileWatcher = FileWatcher(fileManager, collectionManager)
    Thread(fileWatcher, "file-watcher").also { it.isDaemon = true }.start()

    val healthCheckListener = HealthCheckListener(config.port + config.healthPortOffset)
    Thread(healthCheckListener, "health-check-listener").also { it.isDaemon = true }.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Завершение работы сервера — сохранение коллекции...")
        requestManager.saveCollection()
        requestManager.clearVersionOnShutdown()
    })

    logger.info("Загружено {} элементов из файла '{}'", loadedCollection.size, saveFileName)

    val server = RUDPServer(Assembler(), requestManager, config.port, config.maxRetries, config.socketTimeoutMs)
    server.runner()
}