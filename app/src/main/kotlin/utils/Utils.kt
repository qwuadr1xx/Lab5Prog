package ru.qwuadrixx.app.utils

import ru.qwuadrixx.app.commands.Command
import ru.qwuadrixx.app.managers.ICollectionManager
import java.time.LocalDateTime

class PrettyMap(
    private val innerMap: MutableMap<String, Command> = mutableMapOf()
) : MutableMap<String, Command> by innerMap {

    override fun toString(): String {
        return innerMap.entries.joinToString(separator = "\n") { (key: String, value: Command) ->
            "$key : ${value.description}"
        }
    }
}

class CollectionInfo(
    private val collectionManager: ICollectionManager,
    private val collectionClass: String = collectionManager.collection.javaClass.simpleName,
    private val lastInitTime: LocalDateTime = collectionManager.lastInitTime,
    private val lastEditTime: LocalDateTime = collectionManager.lastEditTime,
    private val size: Int = collectionManager.collection.size
) {
    override fun toString(): String {
        return "Тип коллекции: $collectionClass, последнее время инициализации: $lastInitTime, " +
                "последнее время изменения: $lastEditTime, размер коллекции: $size"
    }
}

enum class ExitCode {
    OK,
    ERROR,
    EXIT
}