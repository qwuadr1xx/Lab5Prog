package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

class CountGreaterThanAverageMark(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(
        name = "count_greater_than_average_mark",
        description = "Вывести количество элементов, значение поля averageMark которых меньше заданного"
    ) {
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
}