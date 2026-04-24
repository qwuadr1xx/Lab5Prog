package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.managers.ICommandManager
import utils.ExitCode

/**
 * Команда help
 * @author qwuadrixx
 */
class Help(
    private val console: IConsole, private val commandManager: ICommandManager
) : Command(name = "help", description = "Вывести справку по доступным командам") {

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды help")
        try {
            console.printObject(commandManager.commands)

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }

}