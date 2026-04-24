package ru.qwuadrixx.app.commands

import exception.ScriptRecursionException
import net.requests.ExecuteScriptRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import utils.ExitCode
import java.io.File
import java.io.FileNotFoundException

/**
 * Команда execute_script
 * @author qwuadrixx
 */
class ExecuteScript(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
) : Command(name = "execute_script", description = "Считать и исполнить скрипт из указанного файла.") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды execute_script")
        console.printLine("Введите путь до файла:")

        var filePath: String
        while (true) {
            filePath = console.readLine()
            if (filePath.isNotEmpty()) break
            console.printLine("Имя файла не может быть пустым. Попробуйте снова:")
        }

        try {
            val file = File(filePath)
            if (!file.exists()) throw FileNotFoundException("Файл $filePath не найден")
            if (!activeScripts.add(filePath)) throw ScriptRecursionException("Рекурсия с файлом $filePath")

            val flatLines = expandScriptFile(file)
            activeScripts.remove(filePath)

            if (flatLines.isEmpty()) return ExitCode.OK

            val response = rudpClient.sendAndReceive(ExecuteScriptRequest(flatLines)) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode

        } catch (e: ScriptRecursionException) {
            console.printError(e)
            activeScripts.remove(filePath)
        } catch (e: FileNotFoundException) {
            console.printError(e)
        } catch (e: SecurityException) {
            console.printError(e)
            console.printLine("Недостаточно прав для чтения из файла '$filePath'.")
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }

    private fun expandScriptFile(file: File): List<String> {
        val rawLines = file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val expandedLines = mutableListOf<String>()
        var index = 0

        while (index < rawLines.size) {
            val currentLine = rawLines[index]

            if (currentLine == "execute_script" && index + 1 < rawLines.size) {
                val nestedFilePath = rawLines[index + 1]
                index += 2

                val nestedFile = File(nestedFilePath)
                if (!nestedFile.exists()) throw FileNotFoundException("Вложенный файл скрипта не найден: $nestedFilePath")
                if (!activeScripts.add(nestedFilePath)) throw ScriptRecursionException("Рекурсия с файлом $nestedFilePath")

                expandedLines.addAll(expandScriptFile(nestedFile))
                activeScripts.remove(nestedFilePath)
            } else {
                expandedLines.add(currentLine)
                index++
            }
        }

        return expandedLines
    }

    companion object {
        val activeScripts: MutableSet<String> = HashSet()
    }
}
