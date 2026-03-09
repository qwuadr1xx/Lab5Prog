package ru.qwuadrixx.app.utils

import ru.qwuadrixx.app.managers.ICollectionManager
import java.time.LocalDateTime

class PrettyMap<K, V>(
    private val innerMap: MutableMap<K, V> = mutableMapOf()
) : MutableMap<K, V> by innerMap {

    override fun toString(): String {
        return innerMap.entries.joinToString(separator = "\n") { (key, value) ->
            "$key : $value"
        }
    }
}

class CollectionInfo(
    private val collectionManager: ICollectionManager,
    private val collectionClass: String = collectionManager.collection.javaClass.toString(),
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