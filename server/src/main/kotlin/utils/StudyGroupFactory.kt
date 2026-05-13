package ru.qwuadrixx.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import models.StudyGroup
import java.util.concurrent.atomic.AtomicInteger

object StudyGroupFactory {

    private val seq = AtomicInteger(0)

    private fun nextId(): Int = seq.incrementAndGet()

    fun syncIdGenerator(maxExistingId: Int) {
        seq.updateAndGet { current -> maxOf(current, maxExistingId) }
    }

    fun decrementId() { seq.decrementAndGet() }

    fun assignServerFields(group: StudyGroup): StudyGroup = group.copy(
        id = nextId(),
        creationDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )

    fun forUpdate(group: StudyGroup, targetId: Int, originalCreationDate: LocalDateTime): StudyGroup =
        group.copy(id = targetId, creationDate = originalCreationDate)
}
