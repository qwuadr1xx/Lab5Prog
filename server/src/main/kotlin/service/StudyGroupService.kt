package ru.qwuadrixx.service

import models.StudyGroup
import ru.qwuadrixx.managers.ICollectionManager
import ru.qwuadrixx.managers.IInMemoryCollection
import ru.qwuadrixx.repository.IStudyGroupRepository
import ru.qwuadrixx.utils.StudyGroupFactory
import java.time.LocalDateTime
import java.util.Vector

class StudyGroupService(
    private val repo: IStudyGroupRepository,
    private val inMemory: IInMemoryCollection
) : ICollectionManager {

    override val collection: Vector<StudyGroup> get() = inMemory.collection
    override val lastInitTime: LocalDateTime get() = inMemory.lastInitTime
    override val lastEditTime: LocalDateTime get() = inMemory.lastEditTime
    override var scriptMode: Boolean = false

    override fun add(studyGroup: StudyGroup, userId: Long) {
        if (scriptMode) {
            inMemory.add(StudyGroupFactory.assignServerFields(studyGroup))
        } else {
            val saved = repo.add(studyGroup, userId)
            inMemory.add(saved)
            StudyGroupFactory.syncIdGenerator(saved.id)
        }
    }

    override fun updateById(id: Int, studyGroup: StudyGroup, userId: Long) {
        if (scriptMode) {
            val existing = inMemory.findById(id)
                ?: throw NoSuchElementException("Элемент с id $id не найден")
            inMemory.updateById(id, StudyGroupFactory.forUpdate(studyGroup, id, existing.creationDate))
        } else {
            val updated = repo.updateById(id, studyGroup, userId)
            inMemory.updateById(id, updated)
        }
    }

    override fun removeById(id: Int, userId: Long) {
        if (!scriptMode) repo.removeById(id, userId)
        inMemory.removeById(id)
    }

    override fun clear(userId: Long) {
        if (!scriptMode) repo.clear(userId)
        inMemory.clear()
    }

    override fun insertAt(index: Int, studyGroup: StudyGroup, userId: Long) {
        if (scriptMode) {
            inMemory.insertAt(index, StudyGroupFactory.assignServerFields(studyGroup))
        } else {
            val saved = repo.insertAt(index, studyGroup, userId)
            inMemory.insertAt(index, saved)
            StudyGroupFactory.syncIdGenerator(saved.id)
        }
    }

    override fun removeLast(userId: Long) {
        if (!scriptMode) repo.removeLast(userId)
        inMemory.removeLast()
    }

    override fun addIfMax(studyGroup: StudyGroup, userId: Long): Boolean {
        if (scriptMode) return inMemory.addIfMax(studyGroup)
        val saved = repo.addIfMax(studyGroup, userId) ?: return false
        inMemory.add(saved)
        StudyGroupFactory.syncIdGenerator(saved.id)
        return true
    }

    override fun applyScriptDiff(before: List<StudyGroup>, userId: Long) {
        val after = synchronized(inMemory.collection) { inMemory.collection.toList() }
        val beforeIds = before.map { it.id }.toSet()
        val afterIds = after.map { it.id }.toSet()

        val added = after.filter { it.id !in beforeIds }
        val removedIds = before.filter { it.id !in afterIds }.map { it.id }
        val updated = after.filter { sg ->
            sg.id in beforeIds && before.first { it.id == sg.id } != sg
        }

        repo.applyDiff(added, removedIds, updated, userId)

        val reloaded = repo.findAll()
        synchronized(inMemory.collection) {
            inMemory.collection.clear()
            inMemory.collection.addAll(reloaded)
        }
        reloaded.maxOfOrNull { it.id }?.let { StudyGroupFactory.syncIdGenerator(it) }
        inMemory.lastEditTime = LocalDateTime.now()
    }

    override fun getAverageMarkFromAll(): Long = inMemory.getAverageMarkFromAll()

    override fun countAverageMarkLessThen(averageMark: Long): Long =
        inMemory.countAverageMarkLessThen(averageMark)

    override fun countAverageMarkGreaterThen(averageMark: Long): Long =
        inMemory.countAverageMarkGreaterThen(averageMark)

    override fun takeSnapshot(): List<StudyGroup> = inMemory.takeSnapshot()

    override fun restoreSnapshot(snapshot: List<StudyGroup>) = inMemory.restoreSnapshot(snapshot)

    override fun reloadFromDb() {
        val reloaded = repo.findAll()
        synchronized(inMemory.collection) {
            inMemory.collection.clear()
            inMemory.collection.addAll(reloaded)
        }
        reloaded.maxOfOrNull { it.id }?.let { StudyGroupFactory.syncIdGenerator(it) }
        inMemory.lastEditTime = LocalDateTime.now()
    }
}