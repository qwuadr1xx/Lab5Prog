package ru.qwuadrixx.app

import com.charleskorn.kaml.Yaml
import exception.CommandNotFoundException
import kotlinx.serialization.Serializable
import net.assembler.Assembler
import org.slf4j.LoggerFactory
import ru.qwuadrixx.app.client.RUDPClient
import ru.qwuadrixx.app.commands.*
import ru.qwuadrixx.app.console.Console
import ru.qwuadrixx.app.managers.CommandManager
import utils.ExitCode
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("App")

@Serializable
private data class ClientConfig(
    val serverHost: String = "localhost",
    val serverPort: Int = 8081,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000
)

private fun loadConfig(): ClientConfig {
    val stream = object {}.javaClass.getResourceAsStream("/client.yml")
        ?: return ClientConfig().also { logger.warn("client.yml не найден, используются значения по умолчанию") }
    return try {
        Yaml.default.decodeFromString(ClientConfig.serializer(), stream.bufferedReader().readText())
            .also { logger.info("Конфигурация клиента: host={}, port={}", it.serverHost, it.serverPort) }
    } catch (e: Exception) {
        logger.error("Ошибка загрузки client.yml: {}", e.message)
        ClientConfig()
    }
}

/**
 * Метод входа в программу
 * @author qwuadrixx
 */
@kotlin.uuid.ExperimentalUuidApi
fun main() {
    val config = loadConfig()
    val console = Console()
    val rudpClient = RUDPClient(Assembler(), config.serverHost, config.serverPort, config.maxRetries, config.socketTimeoutMs)
    val commandManager = CommandManager()

    commandManager.apply {
        register(Add(rudpClient, console))
        register(AddIfMax(rudpClient, console))
        register(Show(rudpClient, console))
        register(AverageOfAverageMark(rudpClient, console))
        register(Clear(rudpClient, console))
        register(CountLessThanAverageMark(rudpClient, console))
        register(CountGreaterThanAverageMark(rudpClient, console))
        register(ExecuteScript(rudpClient, console))
        register(Exit(console))
        register(Help(console, commandManager))
        register(Info(rudpClient, console))
        register(InsertAt(rudpClient, console))
        register(RemoveById(rudpClient, console))
        register(RemoveLast(rudpClient, console))
        register(Update(rudpClient, console))
        register(Undo(rudpClient, console))
    }

    while (true) {
        try {
            console.printLine("Введите команду:")
            val commandName = console.readLine()
            val command = commandManager.getCommand(commandName)
            val exitCode = command.execute()

            when (exitCode) {
                ExitCode.EXIT -> exitProcess(0)
                ExitCode.ERROR -> console.printLine("Команда ${command.name} не выполнена")
                ExitCode.OK -> console.printLine("Команда ${command.name} выполнена успешно")
            }
        } catch (e: CommandNotFoundException) {
            console.printError(e)
        }
    }
}
