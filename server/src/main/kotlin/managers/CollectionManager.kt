package ru.qwuadrixx.managers

import models.StudyGroup
import ru.qwuadrixx.utils.StudyGroupFactory
import java.time.LocalDateTime
import java.util.Vector

class CollectionManager : IInMemoryCollection {

    override val collection: Vector<StudyGroup> = Vector()
    override var lastInitTime: LocalDateTime = LocalDateTime.now()
    override var lastEditTime: LocalDateTime = LocalDateTime.now()

    override fun add(studyGroup: StudyGroup) {
        synchronized(collection) { collection.add(studyGroup) }
        lastEditTime = LocalDateTime.now()
    }

    override fun findById(id: Int): StudyGroup? =
        synchronized(collection) { collection.firstOrNull { it.id == id } }

    override fun updateById(id: Int, studyGroup: StudyGroup) {
        synchronized(collection) {
            val index = collection.indexOfFirst { it.id == id }
            if (index == -1) throw NoSuchElementException("Элемент с id $id не найден")
            collection[index] = studyGroup
        }
        lastEditTime = LocalDateTime.now()
    }

    override fun removeById(id: Int) {
        synchronized(collection) { collection.removeIf { it.id == id } }
        lastEditTime = LocalDateTime.now()
    }

    override fun clear() {
        synchronized(collection) { collection.clear() }
        lastEditTime = LocalDateTime.now()
    }

    override fun insertAt(index: Int, studyGroup: StudyGroup) {
        synchronized(collection) { collection.add(index, studyGroup) }
        lastEditTime = LocalDateTime.now()
    }

    override fun removeLast() {
        synchronized(collection) {
            if (collection.isEmpty()) throw NoSuchElementException("Коллекция пуста")
            collection.removeLast()
        }
        lastEditTime = LocalDateTime.now()
    }

    override fun addIfMax(studyGroup: StudyGroup): Boolean =
        synchronized(collection) {
            val isMax = collection.none { it >= studyGroup }
            if (isMax) {
                collection.add(StudyGroupFactory.assignServerFields(studyGroup))
                lastEditTime = LocalDateTime.now()
            }
            isMax
        }

    override fun takeSnapshot(): List<StudyGroup> =
        synchronized(collection) { collection.toList() }

    override fun restoreSnapshot(snapshot: List<StudyGroup>) {
        synchronized(collection) {
            collection.clear()
            collection.addAll(snapshot)
        }
        lastEditTime = LocalDateTime.now()
    }

    override fun getAverageMarkFromAll(): Long = synchronized(collection) {
        if (collection.isEmpty()) 0L
        else collection.map { it.averageMark }.average().toLong()
    }

    override fun countAverageMarkLessThen(averageMark: Long): Long =
        synchronized(collection) { collection.count { it.averageMark < averageMark }.toLong() }

    override fun countAverageMarkGreaterThen(averageMark: Long): Long =
        synchronized(collection) { collection.count { it.averageMark > averageMark }.toLong() }
}