package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

/**
 * Команда count_less_than_average_mark
 * @author qwuadrixx
 */
class CountLessThanAverageMark(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(
        name = "count_less_than_average_mark",
        description = "Вывести количество элементов, значение поля averageMark которых меньше заданного"
    ) {
    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды count_less_than_average_mark")

        while (true) {
            try {
                console.printLine("Введите среднюю оценку:")
                val averageMark = console.readLine().toLong()

                val count = collectionManager.countAverageMarkLessThen(averageMark)
                console.printObject(count)

                return ExitCode.OK
            } catch (e: NumberFormatException) {
                console.printError(e)
                console.printLine("Введите корректное число")
            }
        }
    }

    /**
     * Метод отмены команды
     * @return ExitCode
     */
    override fun undo(): ExitCode = ExitCode.OK
}