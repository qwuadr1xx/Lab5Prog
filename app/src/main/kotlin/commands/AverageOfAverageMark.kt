package ru.qwuadrixx.app.commands

import net.requests.AverageOfAverageMarkRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import utils.ExitCode

/**
 * Команда average_of_average_mark
 * @author qwuadrixx
 */
class AverageOfAverageMark(private val rudpClient: IRUDPClient, private val console: IConsole) : Command(
    name = "average_of_average_mark",
    description = "Вывести среднее значение поля averageMark для всех элементов коллекции"
) {
    override fun execute(): ExitCode {
        console.printLine("Использование команды average_of_average_mark")
        try {
            val response = rudpClient.sendAndReceive(AverageOfAverageMarkRequest()) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}