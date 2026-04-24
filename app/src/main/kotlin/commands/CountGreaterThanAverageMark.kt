package ru.qwuadrixx.app.commands

import net.requests.CountGreaterThanAverageMarkRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import utils.ExitCode

/**
 * Команда count_greater_than_average_mark
 * @author qwuadrixx
 */
class CountGreaterThanAverageMark(private val rudpClient: IRUDPClient, private val console: IConsole) :
    Command(
        name = "count_greater_than_average_mark",
        description = "Вывести количество элементов, значение поля averageMark которых больше заданного"
    ) {

    override fun execute(): ExitCode {
        console.printLine("Использование команды count_greater_than_average_mark")
        while (true) {
            try {
                console.printLine("Введите среднюю оценку:")
                val averageMark = console.readLine().toLong()
                val response = rudpClient.sendAndReceive(
                    CountGreaterThanAverageMarkRequest(averageMark)
                ) as CommandResponse
                if (response.message.isNotEmpty()) console.printObject(response.message)
                return response.exitCode
            } catch (e: NumberFormatException) {
                console.printError(e)
                console.printLine("Введите корректное число")
            } catch (e: Exception) {
                console.printError(e)
                return ExitCode.ERROR
            }
        }
    }
}