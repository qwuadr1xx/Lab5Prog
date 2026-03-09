package ru.qwuadrixx.app.managers

import ru.qwuadrixx.app.models.StudyGroup
import java.time.LocalDateTime
import java.util.*

interface ICollectionManager {
    val collection: Vector<StudyGroup>
    var lastInitTime: LocalDateTime
    var lastEditTime: LocalDateTime

    fun add(studyGroup: StudyGroup)

    fun updateById(id: Int, studyGroup: StudyGroup)

    fun removeById(id: Int)

    fun clear()

    fun saveCollectionToFile()

    fun getCollection(): Collection<StudyGroup>

    fun insertAt(index: Int, studyGroup: StudyGroup)

    fun removeLast()

    fun addIfMax(studyGroup: StudyGroup)

    fun getAverageMarkFromAll(): Long

    fun countAverageMarkLessThen(averageMark: Long): Long

    fun countAverageMarkGreaterThen(averageMark: Long): Long

    fun saveSnapshot(collection: Collection<StudyGroup>)

    fun loadSnapshot(): Collection<StudyGroup>
}