package ru.qwuadrixx.app

import exception.CommandNotFoundException
import org.koin.core.context.startKoin
import ru.qwuadrixx.app.console.Console
import ru.qwuadrixx.app.di.appModule
import ru.qwuadrixx.app.managers.CommandManager
import utils.ExitCode
import kotlin.system.exitProcess

/**
 * Метод входа в программу
 * @author qwuadrixx
 */
@kotlin.uuid.ExperimentalUuidApi
fun main() {
    val koin = startKoin {
        modules(appModule)
    }.koin

    val console = koin.get<Console>()
    val commandManager = koin.get<CommandManager>()

    while (true) {
        try {
            console.printLine("Введите команду:")
            val commandName = console.readLine()
            val command = commandManager.getCommand(commandName)
            val exitCode = command.execute()

            when (exitCode) {
                ExitCode.EXIT -> exitProcess(0)
                ExitCode.ERROR -> console.printLine("Команда ${command.name} не выполнена")
                ExitCode.OK -> console.printLine("Команда ${command.name} выполнена успешно")
            }
        } catch (e: CommandNotFoundException) {
            console.printError(e)
        }
    }
}