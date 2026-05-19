package ru.qwuadrixx.app.commands

import net.requests.CountLessThanAverageMarkRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

/**
 * Команда count_less_than_average_mark
 * @author qwuadrixx
 */
class CountLessThanAverageMark(private val rudpClient: IRUDPClient, private val console: IConsole, private val session: UserSession) :
    Command(
        name = "count_less_than_average_mark",
        description = "Вывести количество элементов, значение поля averageMark которых меньше заданного"
    ) {

    override fun execute(): ExitCode {
        console.printLine("Использование команды count_less_than_average_mark")
        while (true) {
            try {
                console.printLine("Введите среднюю оценку:")
                val averageMark = console.readLine().toLong()
                val response = rudpClient.sendAndReceive(
                    CountLessThanAverageMarkRequest(averageMark, session.token)
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