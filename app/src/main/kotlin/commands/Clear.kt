package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

class Clear(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "clear", description = "Очистить коллекцию") {
    override fun execute(): ExitCode {
        console.printLine("Использование команды clear")
        try {
            collectionManager.clear()

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}