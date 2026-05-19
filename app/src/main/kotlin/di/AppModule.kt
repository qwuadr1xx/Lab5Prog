package ru.qwuadrixx.app.di

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import net.assembler.Assembler
import net.assembler.IAssembler
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.client.RUDPClient
import ru.qwuadrixx.app.commands.*
import ru.qwuadrixx.app.console.Console
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.managers.CommandManager
import ru.qwuadrixx.app.session.UserSession
import kotlin.uuid.ExperimentalUuidApi

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

@OptIn(ExperimentalUuidApi::class)
val appModule = module {
    single { loadClientConfig() }
    single<IAssembler> { Assembler() }
    single<IConsole> { Console() }
    single<IRUDPClient> { RUDPClient(get()) }
    single { UserSession() }
    single {
        val client = get<IRUDPClient>()
        val console = get<IConsole>()
        val session = get<UserSession>()
        CommandManager().apply {
            register(Login(client, console, session))
            register(Register(client, console, session))
            register(Add(client, console, session))
            register(AddIfMax(client, console, session))
            register(Show(client, console, session))
            register(AverageOfAverageMark(client, console, session))
            register(Clear(client, console, session))
            register(CountLessThanAverageMark(client, console, session))
            register(CountGreaterThanAverageMark(client, console, session))
            register(ExecuteScript(client, console, session))
            register(Exit(console))
            register(Help(console, this))
            register(Info(client, console, session))
            register(InsertAt(client, console, session))
            register(RemoveById(client, console, session))
            register(RemoveLast(client, console, session))
            register(Update(client, console, session))
            register(Undo(client, console, session))
            register(ListServers(client, console, session))
            register(EnableServer(client, console, session))
            register(DisableServer(client, console, session))
            register(AddServer(client, console, session))
            register(RemoveServer(client, console, session))
        }
    }
}