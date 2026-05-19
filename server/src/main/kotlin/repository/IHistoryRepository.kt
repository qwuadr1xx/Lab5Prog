package ru.qwuadrixx.repository

import models.StudyGroup
import org.jooq.DSLContext

interface IHistoryRepository {
    val dslContext: DSLContext

    fun push(snapshot: List<StudyGroup>, userId: Long)
    fun popAt(steps: Int): List<StudyGroup>?
}