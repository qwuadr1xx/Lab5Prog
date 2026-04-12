package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.managers.ICollectionManager
import utils.ExitCode

/**
 * Команда show
 * @author qwuadrixx
 */
class Show(private val collectionManager: ICollectionManager, private val console: IConsole) : Command(
    name = "show",
    description = "Вывести в стандартный поток вывода все элементы коллекции в строковом представлении"
) {
    /**
     * Метод исполнения команды
     * @return ExitCode
     */
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

    /**
     * Метод отмены команды
     * @return ExitCode
     */
    override fun undo(): ExitCode = ExitCode.OK

    /**
     * Метод, создающий полную копию команды
     * @return Command
     */
    override fun deepCopy(): Command = Show(collectionManager, console)
}