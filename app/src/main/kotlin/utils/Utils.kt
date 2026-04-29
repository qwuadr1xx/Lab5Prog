package ru.qwuadrixx.app.utils

import ru.qwuadrixx.app.commands.Command

class PrettyMap(
    private val innerMap: MutableMap<String, Command> = mutableMapOf()
) : MutableMap<String, Command> by innerMap {

    override fun toString(): String {
        return innerMap.entries.joinToString(separator = "\n") { (key: String, value: Command) ->
            "$key : ${value.description}"
        }
    }
}