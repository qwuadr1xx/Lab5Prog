package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

/**
 * Команда remove_last
 * @author qwuadrixx
 */
class RemoveLast(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "remove_last", description = "Удалить последний элемент из коллекции") {
    private var studyGroup: StudyGroup? = null

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды remove_last")
        try {
            studyGroup = collectionManager.collection.lastElement()
            collectionManager.removeLast()

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }

    /**
     * Метод отмены команды
     * @return ExitCode
     */
    override fun undo(): ExitCode {
        console.printLine("Отмена команды remove_last")
        try {
            collectionManager.collection.addElement(studyGroup!!)

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}