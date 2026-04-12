package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.CollectionInfo
import utils.ExitCode

/**
 * Команда info
 * @author qwuadrixx
 */
class Info(private val collectionManager: ICollectionManager, private val console: IConsole) : Command(
    name = "info",
    description = "Вывести в стандартный поток вывода информацию о коллекции " +
            "(тип, дата инициализации, количество элементов и т.д.)"
) {
    /**
     * Метод исполнения команды
     * @return ExitCode
     */
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

    /**
     * Метод отмены команды
     * @return ExitCode
     */
    override fun undo(): ExitCode = ExitCode.OK

    /**
     * Метод, создающий полную копию команды
     * @return Command
     */
    override fun deepCopy(): Command = Info(collectionManager, console)
}