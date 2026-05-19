package ru.qwuadrixx.repository

import kotlinx.serialization.ExperimentalSerializationApi

import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import models.StudyGroup
import org.jooq.DSLContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.qwuadrixx.generated.tables.references.SNAPSHOT_HISTORY
import utils.AppProtoBuf

@OptIn(ExperimentalSerializationApi::class)
class HistoryRepository : KoinComponent, IHistoryRepository {

    override val dslContext: DSLContext by inject()

    override fun push(snapshot: List<StudyGroup>, userId: Long) {
        val bytes = AppProtoBuf.encodeToByteArray<List<StudyGroup>>(snapshot)
        dslContext.insertInto(SNAPSHOT_HISTORY)
            .set(SNAPSHOT_HISTORY.PAYLOAD, bytes)
            .set(SNAPSHOT_HISTORY.AUTHOR_ID, userId)
            .execute()
    }

    override fun popAt(steps: Int): List<StudyGroup>? {
        val record = dslContext.selectFrom(SNAPSHOT_HISTORY)
            .orderBy(SNAPSHOT_HISTORY.ID.desc())
            .offset(steps - 1)
            .limit(1)
            .fetchOne() ?: return null

        dslContext.deleteFrom(SNAPSHOT_HISTORY)
            .where(SNAPSHOT_HISTORY.ID.ge(record.id))
            .execute()

        return AppProtoBuf.decodeFromByteArray<List<StudyGroup>>(record.payload!!)
    }
}