package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.exception.ScriptRecursionException
import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.managers.ICommandManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import java.io.FileNotFoundException

class ExecuteScript(
    private val commandManager: ICommandManager,
    private val collectionManager: ICollectionManager,
    private val console: IConsole
) :
    Command(name = "execute_script", description = "Считать и исполнить скрипт из указанного файла.") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды execute_script")

        console.printLine("Введите путь до файла:")
        var fileName: String
        while (true) {
            fileName = console.readLine()

            if (fileName.isEmpty()) {
                console.printLine("Имя файла не может быть пустым.")
                console.printLine("Попробуйте снова:")
            } else break
        }

        val prevReader = console.reader
        val snapshotCollection = collectionManager.loadSnapshot()

            try {
                if (activeScripts.contains(fileName)) throw ScriptRecursionException(
                    "Появляется рекурсия, при повторении $fileName"
                )

                activeScripts.add(fileName)

                console.setFileMode(fileName)

                var line = console.readLine()
                while (line.isNotEmpty()) {
                    line = line.trim()
                    if (line.isNotEmpty()) {
                        try {
                            commandManager.getCommand(line).execute()
                        } catch (e: Exception) {
                            console.printError(e)
                        }
                    }
                    line = console.readLine()
                }

                return ExitCode.OK
            } catch (e: FileNotFoundException) {
                console.printError(e)
                collectionManager.saveSnapshot(snapshotCollection)
            } catch (e: SecurityException) {
                console.printError(e)
                console.printLine("Недостаточно прав для чтения из файла '$fileName'.")
                collectionManager.saveSnapshot(snapshotCollection)
            } catch (e: ScriptRecursionException) {
                console.printError(e)
                collectionManager.saveSnapshot(snapshotCollection)
            } finally {
                console.reader.close()
                activeScripts.remove(fileName)

                if (activeScripts.isEmpty()) {
                    console.setInteractiveMode()
                } else {
                    console.reader = prevReader
                }
            }
        return ExitCode.ERROR
    }

    companion object {
        private val activeScripts = HashSet<String>()
    }
}
