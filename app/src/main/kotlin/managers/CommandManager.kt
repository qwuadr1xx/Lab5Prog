package ru.qwuadrixx.app.managers

import ru.qwuadrixx.app.commands.Command
import ru.qwuadrixx.app.exception.CommandNotFoundException
import ru.qwuadrixx.app.utils.PrettyMap
import kotlin.collections.ArrayDeque

class CommandManager(
    override val commands: MutableMap<String, Command> = PrettyMap(),
    override val history: ArrayDeque<String> = ArrayDeque()
) : ICommandManager {
    override fun register(command: Command) {
        commands[command.name] = command
    }

    override fun getCommand(command: String): Command =
        commands.getOrElse(command) { throw CommandNotFoundException("Команда $command не найдена") }


    override fun addToHistory(command: String) {
        history.addLast(command)
    }
}