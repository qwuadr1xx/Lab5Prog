package ru.qwuadrixx.app.commands

import net.requests.EnableServerRequest
import net.responses.CommandResponse
import net.responses.DisableServerResponse
import net.responses.EnableServerResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class EnableServer(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "enable_server", description = "Включить сервер по его id") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды enable_server")
        while (true) {
            try {
                console.printLine("Введите id сервера:")
                val serverId = console.readLine().toInt()
                var response = rudpClient.sendAndReceive(EnableServerRequest(serverId, session.token))
                if (response is CommandResponse) {
                    console.printLine(response.message)
                    return ExitCode.ERROR
                } else {
                    response = response as EnableServerResponse
                }
                if (response.message.isNotEmpty()) console.printLine(response.message)
                return response.exitCode
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}