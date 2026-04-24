package ru.qwuadrixx

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import net.assembler.Assembler
import org.slf4j.LoggerFactory
import ru.qwuadrixx.client.RUDPServer
import ru.qwuadrixx.managers.CollectionManager
import ru.qwuadrixx.managers.FileManager
import ru.qwuadrixx.managers.RequestManager
import java.util.Vector
import kotlin.uuid.ExperimentalUuidApi

private val logger = LoggerFactory.getLogger("Main")

@Serializable
private data class ServerConfig(
    val port: Int = 8081,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000
)

private fun loadConfig(): ServerConfig {
    val stream = object {}.javaClass.getResourceAsStream("/server.yml")
        ?: return ServerConfig().also { logger.warn("server.yml не найден, используются значения по умолчанию") }
    return try {
        Yaml.default.decodeFromString(ServerConfig.serializer(), stream.bufferedReader().readText())
            .also { logger.info("Конфигурация: port={}, maxRetries={}, socketTimeoutMs={}", it.port, it.maxRetries, it.socketTimeoutMs) }
    } catch (e: Exception) {
        logger.error("Ошибка загрузки server.yml: {}", e.message)
        ServerConfig()
    }
}

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val config = loadConfig()
    val saveFileName = System.getenv("SAVE_FILE_NAME") ?: "save.csv"

    val fileManager = FileManager(saveFileName)
    val loadedCollection = fileManager.readCollection() ?: emptyList()
    val collectionManager = CollectionManager(Vector(loadedCollection))
    val requestManager = RequestManager(collectionManager, fileManager)

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Завершение работы сервера — сохранение коллекции...")
        requestManager.saveCollection()
    })

    logger.info("Загружено {} элементов из файла '{}'", loadedCollection.size, saveFileName)

    val server = RUDPServer(Assembler(), requestManager, config.port, config.maxRetries, config.socketTimeoutMs)
    server.runner()
}
