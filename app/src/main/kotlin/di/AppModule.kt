@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.app.di

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import net.assembler.Assembler
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import ru.qwuadrixx.app.client.RUDPClient
import ru.qwuadrixx.app.commands.*
import ru.qwuadrixx.app.console.Console
import ru.qwuadrixx.app.managers.CommandManager

private val log = LoggerFactory.getLogger("AppModule")

@Serializable
data class ClientConfig(
    val serverHost: String = "localhost",
    val serverPort: Int = 8081,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000
)

fun loadClientConfig(): ClientConfig {
    val stream = object {}.javaClass.getResourceAsStream("/client.yml")
        ?: return ClientConfig().also { log.warn("client.yml не найден, используются значения по умолчанию") }
    val base = try {
        Yaml.default.decodeFromString(ClientConfig.serializer(), stream.bufferedReader().readText())
    } catch (e: Exception) {
        log.error("Ошибка загрузки client.yml: {}", e.message)
        ClientConfig()
    }
    val host = System.getenv("BALANCER_HOST") ?: base.serverHost
    val port = System.getenv("BALANCER_PORT")?.toIntOrNull() ?: base.serverPort
    return base.copy(serverHost = host, serverPort = port)
        .also { log.info("Конфигурация клиента: host={}, port={}", it.serverHost, it.serverPort) }
}

val appModule = module {
    single { loadClientConfig() }
    single { Assembler() }
    single { Console() }
    single {
        val config = get<ClientConfig>()
        RUDPClient(get(), config.serverHost, config.serverPort, config.maxRetries, config.socketTimeoutMs)
    }
    single {
        val client = get<RUDPClient>()
        val console = get<Console>()
        CommandManager().apply {
            register(Add(client, console))
            register(AddIfMax(client, console))
            register(Show(client, console))
            register(AverageOfAverageMark(client, console))
            register(Clear(client, console))
            register(CountLessThanAverageMark(client, console))
            register(CountGreaterThanAverageMark(client, console))
            register(ExecuteScript(client, console))
            register(Exit(console))
            register(Help(console, this))
            register(Info(client, console))
            register(InsertAt(client, console))
            register(RemoveById(client, console))
            register(RemoveLast(client, console))
            register(Update(client, console))
            register(Undo(client, console))
        }
    }
}