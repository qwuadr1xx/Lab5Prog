package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

class Exit(private val console: IConsole) :
    Command(name = "exit", description = "Завершить программу (без сохранения в файл)") {
    override fun execute(): ExitCode {
        console.printLine("Использование команды exit")
        try {
            return ExitCode.EXIT
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}