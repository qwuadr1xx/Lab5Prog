package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

class RemoveLast(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "remove_last", description = "Удалить последний элемент из коллекции") {
    override fun execute(): ExitCode {
        console.printLine("Использование команды remove_last")
        try {
            collectionManager.removeLast()

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}