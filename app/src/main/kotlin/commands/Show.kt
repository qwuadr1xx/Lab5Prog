package ru.qwuadrixx.app.commands

import net.requests.ShowRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class Show(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "show", description = "Вывести в стандартный поток вывода все элементы коллекции в строковом представлении") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды show")
        try {
            val response = rudpClient.sendAndReceive(ShowRequest(session.token)) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}
