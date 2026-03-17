package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

/**
 * Команда add
 * @author qwuadrixx
 */
class Add(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "add", description = "Добавить новый элемент в коллекцию") {
    private var id: Int? = null

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды add")
        try {
            val studyGroup = StudyGroupAsker(console).ask()
            this.id = studyGroup.id
            collectionManager.add(studyGroup)

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
        console.printLine("Отмена команды add")
        try {
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