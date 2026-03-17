package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.ensure

/**
 * Команда remove_by_id
 * @author qwuadrixx
 */
class RemoveById(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "remove_by_id", description = "Удалить элемент из коллекции по его id") {
    private var studyGroup: StudyGroup? = null
    private var index: Int? = null

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды remove_by_id")
        while (true) {
            try {
                console.printLine("Введите id(больше 0):")
                val id = console.readLine().toInt()
                ensure(id > 0) { "Значение id должно быть больше 0" }

                index = collectionManager.collection.indexOfFirst { it.id == id }
                studyGroup = collectionManager.collection[index!!]
                collectionManager.removeById(id)

                return ExitCode.OK
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }

    /**
     * Метод отмены команды
     * @return ExitCode
     */
    override fun undo(): ExitCode {
        console.printLine("Отмена команды remove_by_id")
        try {
            collectionManager.insertAt(index!!, studyGroup!!)

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}