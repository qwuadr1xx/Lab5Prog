package ru.qwuadrixx.app.commands

import net.requests.RemoveLastRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class RemoveLast(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "remove_last", description = "Удалить последний элемент из коллекции") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды remove_last")
        try {
            val response = rudpClient.sendAndReceive(RemoveLastRequest(session.login, session.password)) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}
