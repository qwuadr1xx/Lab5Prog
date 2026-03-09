package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.CollectionInfo
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

class Info(private val collectionManager: ICollectionManager, private val console: IConsole) : Command(
    name = "info",
    description = "Вывести в стандартный поток вывода информацию о коллекции " +
            "(тип, дата инициализации, количество элементов и т.д.)"
) {

    override fun execute(): ExitCode {
        console.printLine("Использование команды info")
        try {
            console.printObject(CollectionInfo(collectionManager))

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}