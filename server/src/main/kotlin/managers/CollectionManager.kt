package ru.qwuadrixx.managers

import exception.NotFoundException
import models.StudyGroup
import java.time.LocalDateTime
import java.util.Vector

class CollectionManager(
    override val collection: Vector<StudyGroup> = Vector(),
    override var lastInitTime: LocalDateTime = LocalDateTime.now(),
    override var lastEditTime: LocalDateTime = LocalDateTime.now(),
    override var currentVersion: String = ""
) : ICollectionManager {

    override fun add(studyGroup: StudyGroup) {
        collection.add(studyGroup)
        lastEditTime = LocalDateTime.now()
    }

    override fun updateById(id: Int, studyGroup: StudyGroup) {
        val index = collection.indexOfFirst { it.id == id }
        if (index == -1) throw NotFoundException("Элемент с id: $id не найден")
        collection[index] = studyGroup
        lastEditTime = LocalDateTime.now()
    }

    override fun removeById(id: Int) {
        val index = collection.indexOfFirst { it.id == id }
        if (index == -1) throw NotFoundException("Элемент с id: $id не найден")
        collection.removeAt(index)
        lastEditTime = LocalDateTime.now()
    }

    override fun clear() {
        collection.clear()
        lastEditTime = LocalDateTime.now()
    }

    override fun insertAt(index: Int, studyGroup: StudyGroup) {
        collection.add(index, studyGroup)
        lastEditTime = LocalDateTime.now()
    }

    override fun removeLast() {
        if (collection.isEmpty()) throw NoSuchElementException("Коллекция пуста")
        collection.removeLast()
        lastEditTime = LocalDateTime.now()
    }

    override fun addIfMax(studyGroup: StudyGroup): Boolean {
        val isMax = collection.none { it >= studyGroup }
        if (isMax) {
            collection.add(studyGroup)
            lastEditTime = LocalDateTime.now()
        }
        return isMax
    }

    override fun getAverageMarkFromAll(): Long =
        if (collection.isEmpty()) 0L
        else collection.map { it.averageMark }.average().toLong()

    override fun countAverageMarkLessThen(averageMark: Long): Long =
        collection.count { it.averageMark < averageMark }.toLong()

    override fun countAverageMarkGreaterThen(averageMark: Long): Long =
        collection.count { it.averageMark > averageMark }.toLong()

    override fun takeSnapshot(): List<StudyGroup> = collection.toList()

    override fun restoreSnapshot(snapshot: List<StudyGroup>) {
        collection.clear()
        collection.addAll(snapshot)
        lastEditTime = LocalDateTime.now()
    }
}
