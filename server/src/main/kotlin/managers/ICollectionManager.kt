package ru.qwuadrixx.managers

import models.StudyGroup
import java.time.LocalDateTime
import java.util.Vector

interface ICollectionManager {
    val collection: Vector<StudyGroup>
    val lastInitTime: LocalDateTime
    val lastEditTime: LocalDateTime
    var scriptMode: Boolean
    fun add(studyGroup: StudyGroup, userId: Long)
    fun updateById(id: Int, studyGroup: StudyGroup, userId: Long)
    fun removeById(id: Int, userId: Long)
    fun clear(userId: Long)
    fun insertAt(index: Int, studyGroup: StudyGroup, userId: Long)
    fun removeLast(userId: Long)
    fun addIfMax(studyGroup: StudyGroup, userId: Long): Boolean
    fun getAverageMarkFromAll(): Long
    fun countAverageMarkLessThen(averageMark: Long): Long
    fun countAverageMarkGreaterThen(averageMark: Long): Long
    fun takeSnapshot(): List<StudyGroup>
    fun restoreSnapshot(snapshot: List<StudyGroup>)
    fun applyScriptDiff(before: List<StudyGroup>, userId: Long)
    fun reloadFromDb()
}