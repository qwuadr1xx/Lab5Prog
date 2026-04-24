package ru.qwuadrixx.app.managers

import ru.qwuadrixx.app.commands.Command

interface ICommandManager {
    val commands: MutableMap<String, Command>

    fun register(command: Command)

    fun getCommand(command: String): Command
}