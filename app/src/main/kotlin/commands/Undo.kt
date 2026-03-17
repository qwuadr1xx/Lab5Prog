package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.exception.UndoException
import ru.qwuadrixx.app.managers.ICommandManager
import ru.qwuadrixx.app.utils.ExitCode
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.ensure

/**
 * Команда undo
 * @author qwuadrixx
 */
class Undo(private val commandManager: ICommandManager, private val console: IConsole) :
    Command(name = "undo", description = "Отмена последних n команд") {
    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    override fun execute(): ExitCode {
        console.printLine("Использование команды undo")
        while (true) {
            try {
                console.printLine("Введите количество отмененных команд(больше 0):")
                var n = console.readLine().toInt()
                ensure(n > 0) { "Значение количества отмененных команд должно быть больше 0" }
                if (n > commandManager.history.size) n = commandManager.history.size
                repeat(n) {
                    val command = commandManager.history.removeLast()
                    if (command.undo() == ExitCode.ERROR) throw UndoException("Ошибка с отменой команды ${command.name}")
                }

                return ExitCode.OK
            } catch (e: UndoException) {
                console.printError(e)
                return ExitCode.ERROR
            } catch (e: NoSuchElementException) {
                console.printError(e)
                console.printLine("В истории команд не осталось элементов")
                return ExitCode.ERROR
            } catch (e: Exception) {
                console.printError(e)
                return ExitCode.ERROR
            }
        }
    }

    /**
     * Метод отмены команды
     * @return ExitCode
     */
    override fun undo(): ExitCode = ExitCode.ERROR
}