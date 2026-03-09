package ru.qwuadrixx.app

import ru.qwuadrixx.app.commands.*
import ru.qwuadrixx.app.exception.CommandNotFoundException
import ru.qwuadrixx.app.managers.CollectionManager
import ru.qwuadrixx.app.managers.CommandManager
import ru.qwuadrixx.app.managers.FileManager
import ru.qwuadrixx.app.utils.Console
import ru.qwuadrixx.app.utils.ExitCode
import kotlin.system.exitProcess

fun main() {
    System.setProperty("SAVE_FILE_NAME", "/Users/qwuadrixx/downloads/testEnv.txt")
    val fileName = System.getProperty("SAVE_FILE_NAME")
    val console = Console()
    val commandManager = CommandManager()
    val fileManager = FileManager(console = console, fileName = fileName)
    val collectionManager = CollectionManager(console = console, fileManager = fileManager)

    commandManager.apply {
        register(Add(collectionManager, console))
        register(AddIfMax(collectionManager, console))
        register(AverageOfAverageMark(collectionManager, console))
        register(Clear(collectionManager, console))
        register(CountLessThanAverageMark(collectionManager, console))
        register(CountGreaterThanAverageMark(collectionManager, console))
        register(ExecuteScript(this, collectionManager, console))
        register(Exit(console))
        register(Help(console, this))
        register(Info(collectionManager, console))
        register(InsertAt(collectionManager, console))
        register(RemoveById(collectionManager, console))
        register(RemoveLast(collectionManager, console))
        register(Save(collectionManager, console))
        register(Update(collectionManager, console))
    }

    while (true) {
        try {
            console.printLine("Введите команду:")
            val command = console.readLine()
            val exitCode = commandManager.getCommand(command).execute()

            if (exitCode == ExitCode.EXIT) {
                exitProcess(1)
            }
        } catch (e: CommandNotFoundException) {
            console.printError(e)
        }
    }
}
