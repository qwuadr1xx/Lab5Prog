package ru.qwuadrixx.repository

import models.StudyGroup
import org.jooq.DSLContext

interface IStudyGroupRepository {
    val dslContext: DSLContext

    fun findAll(): List<StudyGroup>
    fun add(studyGroup: StudyGroup, userId: Long): StudyGroup
    fun addIfMax(studyGroup: StudyGroup, userId: Long): StudyGroup?
    fun insertAt(index: Int, studyGroup: StudyGroup, userId: Long): StudyGroup
    fun updateById(id: Int, studyGroup: StudyGroup, userId: Long): StudyGroup
    fun removeById(id: Int, userId: Long)
    fun removeLast(userId: Long)
    fun clear(userId: Long)
    fun applyDiff(
        added: List<StudyGroup>,
        removedIds: List<Int>,
        updated: List<StudyGroup>,
        userId: Long
    )
}