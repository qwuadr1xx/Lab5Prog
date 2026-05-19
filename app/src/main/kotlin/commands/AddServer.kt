package ru.qwuadrixx.app.commands

import net.requests.AddServerRequest
import net.responses.AddServerResponse
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode
import utils.ensure

class AddServer(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "add_server", description = "Добавить новый сервер по адресу host:port") {

    override fun execute(): ExitCode {
        while (true) {
            try {
                console.printLine("Введите порт сервера (Введите — 0 для эфемерного порта):")
                val portInput = console.readLine().trim()
                val port = if (portInput.isEmpty()) 0 else {
                    val p = portInput.toIntOrNull() ?: throw IllegalArgumentException("Порт должен быть числом")
                    ensure(p in 0..65535) { "Порт должен быть в диапазоне 0–65535" }
                    p
                }
                var response = rudpClient.sendAndReceive(AddServerRequest(port, session.token))
                if (response is CommandResponse) {
                    console.printLine(response.message)
                    return ExitCode.ERROR
                } else {
                    response = response as AddServerResponse
                }
                if (response.message.isNotEmpty()) console.printLine(response.message)
                if (response.exitCode == ExitCode.OK) {
                    console.printLine("ID созданного сервера: ${response.serverId}")
                }
                return response.exitCode
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}