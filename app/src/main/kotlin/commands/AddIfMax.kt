package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

/**
 * Команда add_if_max
 * @author qwuadrixx
 */
class AddIfMax(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(
        name = "add_if_max",
        description = "Добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции"
    ) {
    private var id: Int = -1

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды add_if_max")
        try {
            val studyGroup = StudyGroupAsker(console).ask()
            val isAdded = collectionManager.addIfMax(studyGroup)
            if (isAdded) this.id = studyGroup.id

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
        console.printLine("Отмена команды add_if_max")
        try {
            if (id == -1) return ExitCode.OK
            val index = collectionManager.collection.indexOfFirst { it.id == id }

            collectionManager.collection.removeAt(index)
            StudyGroup.decrementId()
            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}