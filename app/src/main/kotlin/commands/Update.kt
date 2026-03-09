package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.ensure

class Update(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "update", description = "Обновить значение элемента коллекции, id которого равен заданному") {
    override fun execute(): ExitCode {
        console.printLine("Использование команды update")
        while (true) {
            try {
                console.printLine("Введите id(больше 0):")
                val id = console.readLine().toInt()
                ensure(id > 0) { "Значение id должно быть больше 0" }

                val studyGroup = StudyGroupAsker(console).ask()

                collectionManager.updateById(id, studyGroup)

                return ExitCode.OK
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}