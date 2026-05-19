package ru.qwuadrixx.app.commands

import net.requests.ClearRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class Clear(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "clear", description = "Очистить коллекцию") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды clear")
        try {
            val response = rudpClient.sendAndReceive(ClearRequest(session.token)) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}
