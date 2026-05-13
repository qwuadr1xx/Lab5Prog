package ru.qwuadrixx.repository

import models.StudyGroup
import org.jooq.DSLContext

interface IStudyGroupRepository {
    val dslContext: DSLContext

    fun findAll(): List<StudyGroup>
    fun add(studyGroup: StudyGroup, login: String, password: String): StudyGroup
    fun addIfMax(studyGroup: StudyGroup, login: String, password: String): StudyGroup?
    fun insertAt(index: Int, studyGroup: StudyGroup, login: String, password: String): StudyGroup
    fun updateById(id: Int, studyGroup: StudyGroup, login: String, password: String): StudyGroup
    fun removeById(id: Int, login: String, password: String)
    fun removeLast(login: String, password: String)
    fun clear(login: String, password: String)
    fun applyDiff(
        added: List<StudyGroup>,
        removedIds: List<Int>,
        updated: List<StudyGroup>,
        login: String,
        password: String
    )
}
