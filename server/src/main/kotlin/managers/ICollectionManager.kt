package ru.qwuadrixx.managers

import models.StudyGroup
import java.time.LocalDateTime
import java.util.Vector

interface ICollectionManager {
    val collection: Vector<StudyGroup>
    val lastInitTime: LocalDateTime
    val lastEditTime: LocalDateTime
    var scriptMode: Boolean
    fun add(studyGroup: StudyGroup, login: String, password: String)
    fun updateById(id: Int, studyGroup: StudyGroup, login: String, password: String)
    fun removeById(id: Int, login: String, password: String)
    fun clear(login: String, password: String)
    fun insertAt(index: Int, studyGroup: StudyGroup, login: String, password: String)
    fun removeLast(login: String, password: String)
    fun addIfMax(studyGroup: StudyGroup, login: String, password: String): Boolean
    fun getAverageMarkFromAll(): Long
    fun countAverageMarkLessThen(averageMark: Long): Long
    fun countAverageMarkGreaterThen(averageMark: Long): Long
    fun takeSnapshot(): List<StudyGroup>
    fun restoreSnapshot(snapshot: List<StudyGroup>)
    fun applyScriptDiff(before: List<StudyGroup>, login: String, password: String)
    fun reloadFromDb()
}
