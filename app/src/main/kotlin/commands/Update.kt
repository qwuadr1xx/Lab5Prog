package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.ensure

/**
 * Команда update
 */
class Update(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "update", description = "Обновить значение элемента коллекции, id которого равен заданному") {
    private var studyGroup: StudyGroup? = null
    private var id: Int = -1

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды update")
        while (true) {
            try {
                console.printLine("Введите id(больше 0):")
                id = console.readLine().toInt()
                ensure(id > 0) { "Значение id должно быть больше 0" }

                studyGroup = StudyGroupAsker(console).ask(id)

                collectionManager.updateById(id, studyGroup!!)

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
        console.printLine("Отмена команды update")
        try {
            collectionManager.updateById(id, studyGroup!!)
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}