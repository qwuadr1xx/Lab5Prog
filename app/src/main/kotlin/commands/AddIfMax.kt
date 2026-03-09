package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

class AddIfMax(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(
        name = "add_if_max",
        description = "Добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции"
    ) {
    override fun execute(): ExitCode {
        console.printLine("Использование команды add_if_max")
        try {
            val studyGroup = StudyGroupAsker(console).ask()
            collectionManager.addIfMax(studyGroup)

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}