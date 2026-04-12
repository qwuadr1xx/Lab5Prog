package ru.qwuadrixx.app.commands

import exception.ScriptErrorException
import exception.ScriptRecursionException
import models.StudyGroup
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.managers.ICommandManager
import ru.qwuadrixx.app.managers.IFileManager
import utils.ExitCode
import java.io.File
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
        if (activeScripts.isEmpty()) prepare()

        try {
            if (!File(fileName).exists()) throw FileNotFoundException("Файл $fileName не найден")

            if (!activeScripts.add(fileName)) throw ScriptRecursionException("Рекурсия с файлом $fileName")

            console.setFileMode(fileName)

            while (true) {
                val raw = console.reader.readLine() ?: break
                val line = raw.trim()
                if (line.isEmpty()) continue

                val exitCode = commandManager.getCommand(line).execute()
                if (exitCode == ExitCode.ERROR) {
                    throw ScriptErrorException("Ошибка при выполнении команды '$line' в скрипте '$fileName'")
                }
                if (exitCode == ExitCode.EXIT) return ExitCode.EXIT
                if (line == "save") fileChanged = true
            }

            return ExitCode.OK
        } catch (e: ScriptErrorException) {
            console.printError(e, e.message ?: "")
            snapshot?.let { collectionManager.saveSnapshot(it) }
        } catch (e: FileNotFoundException) {
            console.printError(e)
            snapshot?.let { collectionManager.saveSnapshot(it) }
        } catch (e: SecurityException) {
            console.printError(e)
            console.printLine("Недостаточно прав для чтения из файла '$fileName'.")
            snapshot?.let { collectionManager.saveSnapshot(it) }
        } catch (e: ScriptRecursionException) {
            console.printError(e)
            snapshot?.let { collectionManager.saveSnapshot(it) }
        } catch (e: Exception) {
            console.printError(e)
            snapshot?.let { collectionManager.saveSnapshot(it) }
        } finally {
            if (console.fileMode) console.reader.close()

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

            if (fileChanged && byteArray != null) fileManager.writeBytes(byteArray!!)

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }

    /**
     * Метод, создающий полную копию команды
     * @return Command
     */
    override fun deepCopy(): Command = ExecuteScript(commandManager, collectionManager, fileManager, console)

    private fun prepare() {
        snapshot = collectionManager.loadSnapshot()
        byteArray = fileManager.readBytes()
    }

    companion object {
        private val activeScripts = HashSet<String>()
    }
}
