package ru.qwuadrixx.repository

import exception.NotFoundException
import models.Coordinates
import models.Person
import models.StudyGroup
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.qwuadrixx.generated.enums.Semester as GeneratedSemester
import ru.qwuadrixx.generated.tables.references.COORDINATES
import ru.qwuadrixx.generated.tables.references.PERSON
import ru.qwuadrixx.generated.tables.references.STUDY_GROUP
import ru.qwuadrixx.generated.tables.references.USERS
import ru.qwuadrixx.mapper.StudyGroupMapper
import ru.qwuadrixx.utils.hashPassword
import java.time.LocalDateTime
import java.time.ZoneOffset

class StudyGroupRepository : KoinComponent, IStudyGroupRepository {

    override val dslContext: DSLContext by inject()

    private fun verifyAndGetCreatorId(ctx: DSLContext, login: String, password: String): Long =
        ctx.select(USERS.ID)
            .from(USERS)
            .where(USERS.LOGIN.eq(login).and(USERS.PASSWORD_HASH.eq(hashPassword(password))))
            .fetchOptional { it.get(USERS.ID) }
            .orElseThrow { NotFoundException("Пользователь $login не найден.") }!!

    private fun insertCoordinates(ctx: DSLContext, coordinates: Coordinates): Long =
        ctx.insertInto(COORDINATES)
            .set(COORDINATES.X, coordinates.x)
            .set(COORDINATES.Y, coordinates.y)
            .returningResult(COORDINATES.ID)
            .fetchOne { it.get(COORDINATES.ID) }!!

    private fun insertPerson(ctx: DSLContext, person: Person?): Long? =
        person?.let {
            ctx.insertInto(PERSON)
                .set(PERSON.NAME, it.name)
                .set(PERSON.BIRTHDAY, it.birthday?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDate())
                .set(PERSON.HEIGHT, it.height)
                .set(PERSON.PASSPORTID, it.passportID)
                .returningResult(PERSON.ID)
                .fetchOne(PERSON.ID)
        }

    private fun nextPlace(ctx: DSLContext): Long =
        ctx.select(DSL.field("nextval('study_group_place_seq')", Long::class.java))
            .fetchOne()?.value1() ?: throw IllegalStateException("Не удалось получить place из sequence")

    private fun fetchById(ctx: DSLContext, id: Long): StudyGroup {
        val group = ctx.selectFrom(STUDY_GROUP).where(STUDY_GROUP.ID.eq(id)).fetchOne()
            ?: throw NotFoundException("StudyGroup с id=$id не найден")
        val coords = ctx.selectFrom(COORDINATES).where(COORDINATES.ID.eq(group.coordinatesId)).fetchOne()
            ?: throw NotFoundException("Coordinates для id=$id не найдены")
        val person = group.adminId?.let { ctx.selectFrom(PERSON).where(PERSON.ID.eq(it)).fetchOne() }
        return StudyGroupMapper.toDto(group, coords, person)
    }

    private fun doInsert(
        ctx: DSLContext,
        studyGroup: StudyGroup,
        coordId: Long,
        adminId: Long?,
        creatorId: Long,
        place: Long
    ): Long =
        ctx.insertInto(STUDY_GROUP)
            .set(STUDY_GROUP.NAME, studyGroup.name)
            .set(STUDY_GROUP.COORDINATES_ID, coordId)
            .set(STUDY_GROUP.CREATION_DATE, LocalDateTime.now())
            .set(STUDY_GROUP.STUDENTS_COUNT, studyGroup.studentsCount)
            .set(STUDY_GROUP.EXPELLED_STUDENTS, studyGroup.expelledStudents)
            .set(STUDY_GROUP.AVERAGE_MARK, studyGroup.averageMark)
            .set(STUDY_GROUP.SEMESTER_ENUM, studyGroup.semesterEnum?.let { GeneratedSemester.valueOf(it.name) })
            .set(STUDY_GROUP.ADMIN_ID, adminId)
            .set(STUDY_GROUP.CREATOR_ID, creatorId)
            .set(STUDY_GROUP.PLACE, place)
            .returningResult(STUDY_GROUP.ID)
            .fetchOne()?.value1() ?: throw IllegalStateException("Не удалось вставить study_group")

    override fun findAll(): List<StudyGroup> =
        dslContext.selectFrom(STUDY_GROUP)
            .orderBy(STUDY_GROUP.PLACE.asc())
            .fetch()
            .map { group ->
                val coords =
                    dslContext.selectFrom(COORDINATES).where(COORDINATES.ID.eq(group.coordinatesId)).fetchOne()!!
                val person = group.adminId?.let { dslContext.selectFrom(PERSON).where(PERSON.ID.eq(it)).fetchOne() }
                StudyGroupMapper.toDto(group, coords, person)
            }

    override fun add(studyGroup: StudyGroup, login: String, password: String): StudyGroup =
        dslContext.transactionResult { config ->
            val ctx = DSL.using(config)
            val creatorId = verifyAndGetCreatorId(ctx, login, password)
            val coordId = insertCoordinates(ctx, studyGroup.coordinates)
            val adminId = insertPerson(ctx, studyGroup.groupAdmin)
            val newId = doInsert(ctx, studyGroup, coordId, adminId, creatorId, nextPlace(ctx))
            fetchById(ctx, newId)
        }

    override fun addIfMax(studyGroup: StudyGroup, login: String, password: String): StudyGroup? =
        dslContext.transactionResult { config ->
            val ctx = DSL.using(config)
            val creatorId = verifyAndGetCreatorId(ctx, login, password)

            val maxRecord = ctx.selectFrom(STUDY_GROUP)
                .orderBy(STUDY_GROUP.NAME.desc(), STUDY_GROUP.AVERAGE_MARK.desc(), STUDY_GROUP.EXPELLED_STUDENTS.desc())
                .limit(1)
                .forUpdate()
                .fetchOne()

            if (maxRecord != null) {
                val coords = ctx.selectFrom(COORDINATES).where(COORDINATES.ID.eq(maxRecord.coordinatesId)).fetchOne()!!
                val person = maxRecord.adminId?.let { ctx.selectFrom(PERSON).where(PERSON.ID.eq(it)).fetchOne() }
                val existingMax = StudyGroupMapper.toDto(maxRecord, coords, person)
                if (studyGroup <= existingMax) return@transactionResult null
            }

            val coordId = insertCoordinates(ctx, studyGroup.coordinates)
            val adminId = insertPerson(ctx, studyGroup.groupAdmin)
            val newId = doInsert(ctx, studyGroup, coordId, adminId, creatorId, nextPlace(ctx))
            fetchById(ctx, newId)
        }

    override fun insertAt(index: Int, studyGroup: StudyGroup, login: String, password: String): StudyGroup =
        dslContext.transactionResult { config ->
            val ctx = DSL.using(config)
            val creatorId = verifyAndGetCreatorId(ctx, login, password)

            ctx.update(STUDY_GROUP)
                .set(STUDY_GROUP.PLACE, STUDY_GROUP.PLACE.add(1))
                .where(STUDY_GROUP.PLACE.ge(index.toLong()))
                .execute()

            val coordId = insertCoordinates(ctx, studyGroup.coordinates)
            val adminId = insertPerson(ctx, studyGroup.groupAdmin)
            val newId = doInsert(ctx, studyGroup, coordId, adminId, creatorId, index.toLong())

            ctx.execute("SELECT setval('study_group_place_seq', (SELECT MAX(place) FROM study_group))")

            fetchById(ctx, newId)
        }

    override fun updateById(id: Int, studyGroup: StudyGroup, login: String, password: String): StudyGroup =
        dslContext.transactionResult { config ->
            val ctx = DSL.using(config)
            val creatorId = verifyAndGetCreatorId(ctx, login, password)

            val existing = ctx.selectFrom(STUDY_GROUP)
                .where(STUDY_GROUP.ID.eq(id.toLong()).and(STUDY_GROUP.CREATOR_ID.eq(creatorId)))
                .fetchOptional()
                .orElseThrow { NotFoundException("StudyGroup с id $id не найден или нет прав.") }

            ctx.update(COORDINATES)
                .set(COORDINATES.X, studyGroup.coordinates.x)
                .set(COORDINATES.Y, studyGroup.coordinates.y)
                .where(COORDINATES.ID.eq(existing.coordinatesId))
                .execute()

            val newAdminId: Long? = when {
                studyGroup.groupAdmin != null && existing.adminId != null -> {
                    val admin = studyGroup.groupAdmin!!
                    ctx.update(PERSON)
                        .set(PERSON.NAME, admin.name)
                        .set(PERSON.BIRTHDAY, admin.birthday?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDate())
                        .set(PERSON.HEIGHT, admin.height)
                        .set(PERSON.PASSPORTID, admin.passportID)
                        .where(PERSON.ID.eq(existing.adminId))
                        .execute()
                    existing.adminId
                }

                studyGroup.groupAdmin != null && existing.adminId == null ->
                    insertPerson(ctx, studyGroup.groupAdmin)

                else -> {
                    existing.adminId?.let { ctx.deleteFrom(PERSON).where(PERSON.ID.eq(it)).execute() }
                    null
                }
            }

            val javaCreationDate = studyGroup.creationDate.let {
                LocalDateTime.of(it.year, it.monthNumber, it.dayOfMonth, it.hour, it.minute, it.second, it.nanosecond)
            }

            ctx.update(STUDY_GROUP)
                .set(STUDY_GROUP.NAME, studyGroup.name)
                .set(STUDY_GROUP.CREATION_DATE, javaCreationDate)
                .set(STUDY_GROUP.STUDENTS_COUNT, studyGroup.studentsCount)
                .set(STUDY_GROUP.EXPELLED_STUDENTS, studyGroup.expelledStudents)
                .set(STUDY_GROUP.AVERAGE_MARK, studyGroup.averageMark)
                .set(STUDY_GROUP.SEMESTER_ENUM, studyGroup.semesterEnum?.let { GeneratedSemester.valueOf(it.name) })
                .set(STUDY_GROUP.ADMIN_ID, newAdminId)
                .where(STUDY_GROUP.ID.eq(id.toLong()))
                .execute()

            fetchById(ctx, id.toLong())
        }

    override fun removeById(id: Int, login: String, password: String) {
        dslContext.transaction { config ->
            val ctx = DSL.using(config)
            val creatorId = verifyAndGetCreatorId(ctx, login, password)

            val existing = ctx.selectFrom(STUDY_GROUP)
                .where(STUDY_GROUP.ID.eq(id.toLong()).and(STUDY_GROUP.CREATOR_ID.eq(creatorId)))
                .fetchOptional()
                .orElseThrow { NotFoundException("StudyGroup с id $id не найден или нет прав.") }

            existing.adminId?.let { ctx.deleteFrom(PERSON).where(PERSON.ID.eq(it)).execute() }
            ctx.deleteFrom(STUDY_GROUP).where(STUDY_GROUP.ID.eq(id.toLong())).execute()
            ctx.deleteFrom(COORDINATES).where(COORDINATES.ID.eq(existing.coordinatesId)).execute()
        }
    }

    override fun removeLast(login: String, password: String) {
        dslContext.transaction { config ->
            val ctx = DSL.using(config)
            val creatorId = verifyAndGetCreatorId(ctx, login, password)

            val last = ctx.selectFrom(STUDY_GROUP)
                .orderBy(STUDY_GROUP.PLACE.desc())
                .limit(1)
                .forUpdate()
                .fetchOne() ?: throw NoSuchElementException("Коллекция пуста")

            if (last.creatorId != creatorId) throw SecurityException("Нет прав на удаление последнего элемента")

            last.adminId?.let { ctx.deleteFrom(PERSON).where(PERSON.ID.eq(it)).execute() }
            ctx.deleteFrom(STUDY_GROUP).where(STUDY_GROUP.ID.eq(last.id)).execute()
            ctx.deleteFrom(COORDINATES).where(COORDINATES.ID.eq(last.coordinatesId)).execute()
        }
    }

    override fun clear(login: String, password: String) {
        dslContext.transaction { config ->
            val ctx = DSL.using(config)
            val creatorId = verifyAndGetCreatorId(ctx, login, password)

            val groups = ctx.selectFrom(STUDY_GROUP)
                .where(STUDY_GROUP.CREATOR_ID.eq(creatorId))
                .fetch()

            val coordinateIds = groups.mapNotNull { it.coordinatesId }
            val adminIds = groups.mapNotNull { it.adminId }

            ctx.deleteFrom(STUDY_GROUP).where(STUDY_GROUP.CREATOR_ID.eq(creatorId)).execute()
            if (adminIds.isNotEmpty()) ctx.deleteFrom(PERSON).where(PERSON.ID.`in`(adminIds)).execute()
            if (coordinateIds.isNotEmpty()) ctx.deleteFrom(COORDINATES).where(COORDINATES.ID.`in`(coordinateIds)).execute()
        }
    }

    override fun applyDiff(
        added: List<StudyGroup>,
        removedIds: List<Int>,
        updated: List<StudyGroup>,
        login: String,
        password: String
    ) {
        dslContext.transaction { config ->
            val ctx = DSL.using(config)
            val creatorId = verifyAndGetCreatorId(ctx, login, password)

            for (id in removedIds) {
                val existing = ctx.selectFrom(STUDY_GROUP)
                    .where(STUDY_GROUP.ID.eq(id.toLong()).and(STUDY_GROUP.CREATOR_ID.eq(creatorId)))
                    .fetchOne() ?: continue
                existing.adminId?.let { ctx.deleteFrom(PERSON).where(PERSON.ID.eq(it)).execute() }
                ctx.deleteFrom(STUDY_GROUP).where(STUDY_GROUP.ID.eq(id.toLong())).execute()
                ctx.deleteFrom(COORDINATES).where(COORDINATES.ID.eq(existing.coordinatesId)).execute()
            }

            for (sg in updated) {
                val existing = ctx.selectFrom(STUDY_GROUP)
                    .where(STUDY_GROUP.ID.eq(sg.id.toLong()).and(STUDY_GROUP.CREATOR_ID.eq(creatorId)))
                    .fetchOne() ?: continue

                ctx.update(COORDINATES)
                    .set(COORDINATES.X, sg.coordinates.x)
                    .set(COORDINATES.Y, sg.coordinates.y)
                    .where(COORDINATES.ID.eq(existing.coordinatesId))
                    .execute()

                val newAdminId: Long? = when {
                    sg.groupAdmin != null && existing.adminId != null -> {
                        val admin = sg.groupAdmin!!
                        ctx.update(PERSON)
                            .set(PERSON.NAME, admin.name)
                            .set(PERSON.BIRTHDAY, admin.birthday?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDate())
                            .set(PERSON.HEIGHT, admin.height)
                            .set(PERSON.PASSPORTID, admin.passportID)
                            .where(PERSON.ID.eq(existing.adminId))
                            .execute()
                        existing.adminId
                    }
                    sg.groupAdmin != null && existing.adminId == null -> insertPerson(ctx, sg.groupAdmin)
                    else -> {
                        existing.adminId?.let { ctx.deleteFrom(PERSON).where(PERSON.ID.eq(it)).execute() }
                        null
                    }
                }

                val javaCreationDate = sg.creationDate.let {
                    LocalDateTime.of(it.year, it.monthNumber, it.dayOfMonth, it.hour, it.minute, it.second, it.nanosecond)
                }

                ctx.update(STUDY_GROUP)
                    .set(STUDY_GROUP.NAME, sg.name)
                    .set(STUDY_GROUP.CREATION_DATE, javaCreationDate)
                    .set(STUDY_GROUP.STUDENTS_COUNT, sg.studentsCount)
                    .set(STUDY_GROUP.EXPELLED_STUDENTS, sg.expelledStudents)
                    .set(STUDY_GROUP.AVERAGE_MARK, sg.averageMark)
                    .set(STUDY_GROUP.SEMESTER_ENUM, sg.semesterEnum?.let { GeneratedSemester.valueOf(it.name) })
                    .set(STUDY_GROUP.ADMIN_ID, newAdminId)
                    .where(STUDY_GROUP.ID.eq(sg.id.toLong()))
                    .execute()
            }

            for (sg in added) {
                val coordId = insertCoordinates(ctx, sg.coordinates)
                val adminId = insertPerson(ctx, sg.groupAdmin)
                doInsert(ctx, sg, coordId, adminId, creatorId, nextPlace(ctx))
            }
        }
    }
}
