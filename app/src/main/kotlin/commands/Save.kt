package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import java.io.FileNotFoundException

class Save(
    private val collectionManager: ICollectionManager,
    private val console: IConsole
) :
    Command(name = "save", description = "Сохранить коллекцию в файл") {
    override fun execute(): ExitCode {
        console.printLine("Использование команды save")
        try {
            collectionManager.saveCollectionToFile()
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
}