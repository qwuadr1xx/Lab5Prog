package ru.qwuadrixx.app.commands

import utils.ExitCode

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

    /**
     * Метод, создающий полную копию команды
     * @return Command
     */
    abstract fun deepCopy(): Command
}