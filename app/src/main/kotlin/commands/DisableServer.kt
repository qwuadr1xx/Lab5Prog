package ru.qwuadrixx.app.commands

import net.requests.DisableServerRequest
import net.responses.AddServerResponse
import net.responses.CommandResponse
import net.responses.DisableServerResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class DisableServer(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "disable_server", description = "Выключить сервер по его id") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды disable_server")
        while (true) {
            try {
                console.printLine("Введите id сервера:")
                val serverId = console.readLine().toInt()
                var response = rudpClient.sendAndReceive(DisableServerRequest(serverId, session.token))
                if (response is CommandResponse) {
                    console.printLine(response.message)
                    return ExitCode.ERROR
                } else {
                    response = response as DisableServerResponse
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