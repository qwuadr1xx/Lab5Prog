package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.IFileManager
import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import java.io.FileNotFoundException

/**
 * Команда save
 * @author qwuadrixx
 */
class Save(
    private val collectionManager: ICollectionManager,
    private val fileManager: IFileManager,
    private val console: IConsole
) :
    Command(name = "save", description = "Сохранить коллекцию в файл") {
    private var byteArray: ByteArray? = null

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды save")
        try {
            byteArray = fileManager.readBytes()
            fileManager.writeCollection(collectionManager.collection)
            return ExitCode.OK
        } catch (e: FileNotFoundException) {
            console.printError(e)
            console.printLine("Файл не найден, попробуйте ввести валидное название")
        } catch (e: SecurityException) {
            console.printError(e)
            console.printLine("Недостаточно прав для записи в файл")
        }
        return ExitCode.ERROR
    }

    /**
     * Метод отмены команды
     * @return ExitCode
     */
    override fun undo(): ExitCode {
        console.printLine("Отмена команды save")
        try {
            fileManager.writeBytes(byteArray!!)

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}