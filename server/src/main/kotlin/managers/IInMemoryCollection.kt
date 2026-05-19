package ru.qwuadrixx.managers

import models.StudyGroup
import java.time.LocalDateTime
import java.util.Vector

interface IInMemoryCollection {
    val collection: Vector<StudyGroup>
    var lastInitTime: LocalDateTime
    var lastEditTime: LocalDateTime

    fun add(studyGroup: StudyGroup)
    fun findById(id: Int): StudyGroup?
    fun updateById(id: Int, studyGroup: StudyGroup)
    fun removeById(id: Int)
    fun clear()
    fun insertAt(index: Int, studyGroup: StudyGroup)
    fun removeLast()
    fun addIfMax(studyGroup: StudyGroup): Boolean
    fun takeSnapshot(): List<StudyGroup>
    fun restoreSnapshot(snapshot: List<StudyGroup>)
    fun getAverageMarkFromAll(): Long
    fun countAverageMarkLessThen(averageMark: Long): Long
    fun countAverageMarkGreaterThen(averageMark: Long): Long
}