package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

class AverageOfAverageMark(private val collectionManager: ICollectionManager, private val console: IConsole) : Command(
    name = "average_of_average_mark",
    description = "Вывести среднее значение поля averageMark для всех элементов коллекции"
) {
    override fun execute(): ExitCode {
        console.printLine("Использование команды average_of_average_mark")

        try {
            val averageMark = collectionManager.getAverageMarkFromAll()
            console.printObject(averageMark)

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}