package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

class Show(private val collectionManager: ICollectionManager, private val console: IConsole) : Command(
    name = "show",
    description = "Вывести в стандартный поток вывода все элементы коллекции в строковом представлении"
) {
    override fun execute(): ExitCode {
        console.printLine("Использование команды show")
        try {
            console.printObject(collectionManager.collection.joinToString(separator = "\n"))

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}