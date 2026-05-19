package ru.qwuadrixx.app.commands

import net.requests.AverageOfAverageMarkRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class AverageOfAverageMark(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "average_of_average_mark", description = "Вывести среднее значение поля averageMark для всех элементов коллекции") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды average_of_average_mark")
        try {
            val response = rudpClient.sendAndReceive(AverageOfAverageMarkRequest(session.token)) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}
