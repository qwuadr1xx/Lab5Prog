package ru.qwuadrixx.app.commands

import net.requests.RemoveServerRequest
import net.responses.CommandResponse
import net.responses.ListServersResponse
import net.responses.RemoveServerResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class RemoveServer(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "remove_server", description = "Удалить сервер по его id") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды remove_server")
        while (true) {
            try {
                console.printLine("Введите id сервера:")
                val serverId = console.readLine().toInt()
                var response = rudpClient.sendAndReceive(RemoveServerRequest(serverId, session.token))
                if (response is CommandResponse) {
                    console.printLine(response.message)
                    return ExitCode.ERROR
                } else {
                    response = response as RemoveServerResponse
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