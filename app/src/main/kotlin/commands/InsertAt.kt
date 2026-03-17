package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.ensure

/**
 * Команда insert_at
 * @author qwuadrixx
 */
class InsertAt(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "insert_at", description = "Добавить новый элемент в заданную позицию") {
    private var index: Int = -1

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды insert_at")
        while (true) {
            try {
                console.printLine("Введите index(больше 0):")
                val index = console.readLine().toInt()
                this.index = index
                ensure(index > 0) { "Значение index должно быть больше 0" }

                val studyGroup = StudyGroupAsker(console = console).ask()

                collectionManager.insertAt(index, studyGroup)

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
        console.printLine("Отмена команды insert_at")
        try {
            collectionManager.collection.removeAt(index)
            StudyGroup.decrementId()
            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}