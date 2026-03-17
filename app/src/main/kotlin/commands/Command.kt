package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.utils.ExitCode

/**
 * Абстрактный класс-родитель для всех команд
 */
abstract class Command(val name: String, val description: String) {
    /**
     * Метод исполнения команды
     * @return ExitCode
     */
    abstract fun execute(): ExitCode

    /**
     * Метод отмены команды
     * @return ExitCode
     */
    abstract fun undo(): ExitCode
}