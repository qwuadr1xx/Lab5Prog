package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.managers.ICollectionManager
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole

class Add(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "add", description = "Добавить новый элемент в коллекцию") {
    override fun execute(): ExitCode {
        console.printLine("Использование команды add")
        try {
            val studyGroup = StudyGroupAsker(console).ask()
            collectionManager.add(studyGroup)

            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}