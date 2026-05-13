package ru.qwuadrixx.managers

import models.StudyGroup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.qwuadrixx.client.CollectionSyncNotifier
import ru.qwuadrixx.repository.IStudyGroupRepository
import ru.qwuadrixx.utils.StudyGroupFactory
import java.time.LocalDateTime
import java.util.Vector

class CollectionManager : KoinComponent, ICollectionManager {

    private val repo: IStudyGroupRepository by inject()
    private val syncNotifier: CollectionSyncNotifier by inject()

    override val collection: Vector<StudyGroup> = Vector()
    override var lastInitTime: LocalDateTime = LocalDateTime.now()
    override var lastEditTime: LocalDateTime = LocalDateTime.now()
    override var scriptMode: Boolean = false

    init {
        val loaded = repo.findAll()
        synchronized(collection) {
            collection.addAll(loaded)
        }
        loaded.maxOfOrNull { it.id }?.let { StudyGroupFactory.syncIdGenerator(it) }
        lastInitTime = LocalDateTime.now()
    }

    override fun add(studyGroup: StudyGroup, login: String, password: String) {
        if (scriptMode) {
            synchronized(collection) { collection.add(StudyGroupFactory.assignServerFields(studyGroup)) }
        } else {
            val saved = repo.add(studyGroup, login, password)
            synchronized(collection) { collection.add(saved) }
            StudyGroupFactory.syncIdGenerator(saved.id)
            syncNotifier.notifyPeers()
        }
        lastEditTime = LocalDateTime.now()
    }

    override fun updateById(id: Int, studyGroup: StudyGroup, login: String, password: String) {
        if (scriptMode) {
            synchronized(collection) {
                val index = collection.indexOfFirst { it.id == id }
                if (index == -1) throw NoSuchElementException("Элемент с id $id не найден")
                collection[index] = StudyGroupFactory.forUpdate(studyGroup, id, collection[index].creationDate)
            }
        } else {
            val updated = repo.updateById(id, studyGroup, login, password)
            synchronized(collection) {
                val index = collection.indexOfFirst { it.id == id }
                if (index == -1) throw NoSuchElementException("Элемент с id $id не найден")
                collection[index] = updated
            }
            syncNotifier.notifyPeers()
        }
        lastEditTime = LocalDateTime.now()
    }

    override fun removeById(id: Int, login: String, password: String) {
        if (!scriptMode) {
            repo.removeById(id, login, password)
            syncNotifier.notifyPeers()
        }
        synchronized(collection) { collection.removeIf { it.id == id } }
        lastEditTime = LocalDateTime.now()
    }

    override fun clear(login: String, password: String) {
        if (!scriptMode) {
            repo.clear(login, password)
            syncNotifier.notifyPeers()
        }
        synchronized(collection) { collection.clear() }
        lastEditTime = LocalDateTime.now()
    }

    override fun insertAt(index: Int, studyGroup: StudyGroup, login: String, password: String) {
        if (scriptMode) {
            synchronized(collection) { collection.add(index, StudyGroupFactory.assignServerFields(studyGroup)) }
        } else {
            val saved = repo.insertAt(index, studyGroup, login, password)
            synchronized(collection) { collection.add(index, saved) }
            StudyGroupFactory.syncIdGenerator(saved.id)
            syncNotifier.notifyPeers()
        }
        lastEditTime = LocalDateTime.now()
    }

    override fun removeLast(login: String, password: String) {
        synchronized(collection) {
            if (collection.isEmpty()) throw NoSuchElementException("Коллекция пуста")
            if (!scriptMode) {
                repo.removeLast(login, password)
                syncNotifier.notifyPeers()
            }
            collection.removeLast()
        }
        lastEditTime = LocalDateTime.now()
    }

    override fun addIfMax(studyGroup: StudyGroup, login: String, password: String): Boolean {
        if (scriptMode) {
            return synchronized(collection) {
                val isMax = collection.none { it >= studyGroup }
                if (isMax) {
                    collection.add(StudyGroupFactory.assignServerFields(studyGroup))
                    lastEditTime = LocalDateTime.now()
                }
                isMax
            }
        }
        val saved = repo.addIfMax(studyGroup, login, password) ?: return false
        synchronized(collection) { collection.add(saved) }
        StudyGroupFactory.syncIdGenerator(saved.id)
        syncNotifier.notifyPeers()
        lastEditTime = LocalDateTime.now()
        return true
    }

    override fun applyScriptDiff(before: List<StudyGroup>, login: String, password: String) {
        val after = synchronized(collection) { collection.toList() }
        val beforeIds = before.map { it.id }.toSet()
        val afterIds = after.map { it.id }.toSet()

        val added = after.filter { it.id !in beforeIds }
        val removedIds = before.filter { it.id !in afterIds }.map { it.id }
        val updated = after.filter { sg ->
            sg.id in beforeIds && before.first { it.id == sg.id } != sg
        }

        repo.applyDiff(added, removedIds, updated, login, password)

        val reloaded = repo.findAll()
        synchronized(collection) {
            collection.clear()
            collection.addAll(reloaded)
        }
        reloaded.maxOfOrNull { it.id }?.let { StudyGroupFactory.syncIdGenerator(it) }
        lastEditTime = LocalDateTime.now()
        syncNotifier.notifyPeers()
    }

    override fun getAverageMarkFromAll(): Long = synchronized(collection) {
        if (collection.isEmpty()) 0L
        else collection.map { it.averageMark }.average().toLong()
    }

    override fun countAverageMarkLessThen(averageMark: Long): Long = synchronized(collection) {
        collection.count { it.averageMark < averageMark }.toLong()
    }

    override fun countAverageMarkGreaterThen(averageMark: Long): Long = synchronized(collection) {
        collection.count { it.averageMark > averageMark }.toLong()
    }

    override fun takeSnapshot(): List<StudyGroup> = synchronized(collection) { collection.toList() }

    override fun restoreSnapshot(snapshot: List<StudyGroup>) {
        synchronized(collection) {
            collection.clear()
            collection.addAll(snapshot)
        }
        lastEditTime = LocalDateTime.now()
    }

    override fun reloadFromDb() {
        val reloaded = repo.findAll()
        synchronized(collection) {
            collection.clear()
            collection.addAll(reloaded)
        }
        reloaded.maxOfOrNull { it.id }?.let { StudyGroupFactory.syncIdGenerator(it) }
        lastEditTime = LocalDateTime.now()
    }
}
