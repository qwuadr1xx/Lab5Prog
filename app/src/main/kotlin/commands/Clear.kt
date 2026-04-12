package ru.qwuadrixx.app.commands

import models.StudyGroup
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.managers.ICollectionManager
import utils.ExitCode

/**
 * Команда clear
 * @author qwuadrixx
 */
class Clear(private val collectionManager: ICollectionManager, private val console: IConsole) :
    Command(name = "clear", description = "Очистить коллекцию") {
    private var snapshot: Collection<StudyGroup>? = null

    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды clear")
        try {
            this.snapshot = collectionManager.loadSnapshot()
            collectionManager.clear()

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
        console.printLine("Отмена команды clear")
        try {
            collectionManager.saveSnapshot(snapshot!!)
            return ExitCode.OK
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }

    /**
     * Метод, создающий полную копию команды
     * @return Command
     */
    override fun deepCopy(): Command = Clear(collectionManager, console)
}