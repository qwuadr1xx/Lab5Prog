package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.ensure

class RemoveById(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "remove_by_id", description = "Удалить элемент из коллекции по его id") {
    override fun execute(): ExitCode {
        console.printLine("Использование команды remove_by_id")
        while (true) {
            try {
                console.printLine("Введите id(больше 0):")
                val id = console.readLine().toInt()
                ensure(id > 0) { "Значение id должно быть больше 0" }

                collectionManager.removeById(id)

                return ExitCode.OK
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}