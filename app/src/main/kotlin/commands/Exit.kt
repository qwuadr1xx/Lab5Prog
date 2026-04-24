package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.console.IConsole
import utils.ExitCode

/**
 * Команда exit
 * @author qwuadrixx
 */
class Exit(private val console: IConsole) :
    Command(name = "exit", description = "Завершить программу (без сохранения в файл)") {
    override fun execute(): ExitCode {
        console.printLine("Использование команды exit")
        return ExitCode.EXIT
    }
}