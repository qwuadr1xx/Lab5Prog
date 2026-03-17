package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.exception.ScriptErrorException
import ru.qwuadrixx.app.exception.ScriptRecursionException
import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.managers.ICommandManager
import ru.qwuadrixx.app.managers.IFileManager
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import java.io.FileNotFoundException

/**
 * Команда execute_script
 * @author qwuadrixx
 */
class ExecuteScript(
    private val commandManager: ICommandManager,
    private val collectionManager: ICollectionManager,
    private val fileManager: IFileManager,
    private val console: IConsole,
) :
    Command(name = "execute_script", description = "Считать и исполнить скрипт из указанного файла.") {
    private var snapshot: Collection<StudyGroup>? = null
    private var fileChanged: Boolean = false
    private var byteArray: ByteArray? = null

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
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

        if (activeScripts.size == 0) prepare()

        try {
            if (activeScripts.contains(fileName)) throw ScriptRecursionException(
                "Появляется рекурсия, при повторении $fileName"
            )

            activeScripts.add(fileName)

            console.setFileMode(fileName)

            do {
                var line = console.readLine()
                line = line.trim()
                if (line.isNotEmpty()) {
                    if (commandManager.getCommand(line).execute() == ExitCode.ERROR) return ExitCode.ERROR
                    if (line == "save") fileChanged = true
                }
            } while (line.isNotEmpty())

            return ExitCode.OK
        } catch (e: ScriptErrorException) {
            console.printError(e, e.cause?.message ?: "")
            collectionManager.saveSnapshot(snapshotCollection)
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
            if (console.reader != prevReader) console.reader.close()
            activeScripts.remove(fileName)

            if (activeScripts.isEmpty()) {
                console.setInteractiveMode()
            } else {
                console.reader = prevReader
            }
        }
        return ExitCode.ERROR
    }

    /**
     * Метод отмены команды
     * @return ExitCode
     */
    override fun undo(): ExitCode {
        console.printLine("Отмена команды execute_script")
        try {
            collectionManager.saveSnapshot(snapshot!!)

            if (fileChanged) fileManager.writeBytes(byteArray!!)

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }

    private fun prepare() {
        snapshot = collectionManager.loadSnapshot()
        byteArray = fileManager.readBytes()
    }

    companion object {
        private val activeScripts = HashSet<String>()
    }
}
