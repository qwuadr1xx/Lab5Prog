package ru.qwuadrixx.app.commands

import ru.qwuadrixx.app.utils.ExitCode

/**
 * Абстрактный класс-родитель для всех команд
 */
abstract class Command(val name: String, val description: String) {
    abstract fun execute(): ExitCode
}