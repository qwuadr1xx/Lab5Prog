package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.managers.ICollectionManager
import utils.ExitCode

/**
 * Команда count_greater_than_average_mark
 * @author qwuadrixx
 */
class CountGreaterThanAverageMark(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(
        name = "count_greater_than_average_mark",
        description = "Вывести количество элементов, значение поля averageMark которых меньше заданного"
    ) {
    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды count_greater_than_average_mark")

        while (true) {
            try {
                console.printLine("Введите среднюю оценку:")
                val averageMark = console.readLine().toLong()

                val count = collectionManager.countAverageMarkGreaterThen(averageMark)
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

    /**
     * Метод, создающий полную копию команды
     * @return Command
     */
    override fun deepCopy(): Command = CountGreaterThanAverageMark(collectionManager, console)
}