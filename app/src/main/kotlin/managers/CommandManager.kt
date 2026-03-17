package ru.qwuadrixx.app.managers

import ru.qwuadrixx.app.commands.Command
import ru.qwuadrixx.app.exception.CommandNotFoundException
import ru.qwuadrixx.app.utils.PrettyMap
import kotlin.collections.ArrayDeque

/**
 * Класс, отвечающий за хранение команд и взаимодействия с ними
 * @author qwuadrixx
 */
class CommandManager(
    override val commands: MutableMap<String, Command> = PrettyMap(),
    override val history: ArrayDeque<Command> = ArrayDeque()
) : ICommandManager {
    /**
     * Метод, отвечающий за регистрацию команды в словаре
     * @param command
     */
    override fun register(command: Command) {
        commands[command.name] = command
    }

    /**
     * Метод, отвечающий за получение команды по ее имени
     * @param command
     * @return Command
     */
    override fun getCommand(command: String): Command =
        commands.getOrElse(command) { throw CommandNotFoundException("Команда $command не найдена") }

    /**
     * Метод, отвечающий за добавление команды в историю
     * @param command
     */
    override fun addToHistory(command: Command) {
        history.addLast(command)
    }
}