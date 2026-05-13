package ru.qwuadrixx.app

import exception.CommandNotFoundException
import org.koin.core.context.startKoin
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.di.appModule
import ru.qwuadrixx.app.managers.CommandManager
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode
import kotlin.system.exitProcess

private val NO_AUTH_COMMANDS = setOf("login", "register", "exit", "help")

/**
 * Метод входа в программу
 * @author qwuadrixx
 */
@kotlin.uuid.ExperimentalUuidApi
fun main() {
    val koin = startKoin {
        modules(appModule)
    }.koin

    val console = koin.get<IConsole>()
    val commandManager = koin.get<CommandManager>()
    val session = koin.get<UserSession>()

    while (true) {
        try {
            val prompt = if (session.isAuthenticated) "[${session.login}] Введите команду:" else "Введите команду (login/register):"
            console.printLine(prompt)
            val commandName = console.readLine()

            if (!session.isAuthenticated && commandName !in NO_AUTH_COMMANDS) {
                console.printLine("Необходимо авторизоваться. Используйте команды login или register.")
                continue
            }

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