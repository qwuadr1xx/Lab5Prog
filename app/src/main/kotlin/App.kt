package ru.qwuadrixx.app

import ru.qwuadrixx.app.commands.*
import ru.qwuadrixx.app.exception.CommandNotFoundException
import ru.qwuadrixx.app.managers.CollectionManager
import ru.qwuadrixx.app.managers.CommandManager
import ru.qwuadrixx.app.managers.FileManager
import ru.qwuadrixx.app.utils.Console
import ru.qwuadrixx.app.utils.ExitCode
import java.util.*
import kotlin.system.exitProcess

/**
 * Метод входа в программу
 * @author qwuadrixx
 */
fun main() {
    //Самому задать переменную окружения
    val fileName = System.getenv("SAVE_FILE_NAME")
    println(fileName)
    val console = Console()
    val commandManager = CommandManager()
    val fileManager = FileManager(console = console, fileName = fileName)
    val collectionManager =
        CollectionManager(console = console, collection = Vector(fileManager.readCollection() ?: emptyList()))

    commandManager.apply {
        register(Add(collectionManager, console))
        register(AddIfMax(collectionManager, console))
        register(Show(collectionManager, console))
        register(AverageOfAverageMark(collectionManager, console))
        register(Clear(collectionManager, console))
        register(CountLessThanAverageMark(collectionManager, console))
        register(CountGreaterThanAverageMark(collectionManager, console))
        register(ExecuteScript(this, collectionManager, fileManager, console))
        register(Exit(console))
        register(Help(console, this))
        register(Info(collectionManager, console))
        register(InsertAt(collectionManager, console))
        register(RemoveById(collectionManager, console))
        register(RemoveLast(collectionManager, console))
        register(Save(collectionManager, fileManager, console))
        register(Update(collectionManager, console))
        register(Undo(this, console))
    }

    while (true) {
        try {
            console.printLine("Введите команду:")
            val commandName = console.readLine()
            val command = commandManager.getCommand(commandName)
            val exitCode = command.execute()

            when (exitCode) {
                ExitCode.EXIT -> exitProcess(0)
                ExitCode.ERROR -> println("Команда ${command.name} не выполнена")
                ExitCode.OK -> {
                    if (commandName != "undo") commandManager.addToHistory(command)
                    println("Команда ${command.name} выполнена успешно")
                }
            }
        } catch (e: CommandNotFoundException) {
            console.printError(e)
        }
    }
}
