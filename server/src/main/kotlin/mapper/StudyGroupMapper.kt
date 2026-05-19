package ru.qwuadrixx.mapper

import models.Semester
import models.StudyGroup
import ru.qwuadrixx.generated.tables.records.CoordinatesRecord
import ru.qwuadrixx.generated.tables.records.PersonRecord
import ru.qwuadrixx.generated.tables.records.StudyGroupRecord
import kotlinx.datetime.LocalDateTime

object StudyGroupMapper {
    fun toDto(
        group: StudyGroupRecord,
        coordinates: CoordinatesRecord,
        person: PersonRecord?
    ): StudyGroup {
        val javaDate = requireNotNull(group.creationDate) { "study_group.creation_date не может быть null" }
        val creationDate = LocalDateTime(
            javaDate.year, javaDate.monthValue, javaDate.dayOfMonth,
            javaDate.hour, javaDate.minute, javaDate.second, javaDate.nano
        )

        return StudyGroup(
            id = requireNotNull(group.id) { "study_group.id не может быть null" }.toInt(),
            name = requireNotNull(group.name) { "study_group.name не может быть null" },
            coordinates = CoordinatesMapper.toDto(coordinates),
            creationDate = creationDate,
            studentsCount = group.studentsCount,
            expelledStudents = requireNotNull(group.expelledStudents) { "study_group.expelled_students не может быть null" },
            averageMark = requireNotNull(group.averageMark) { "study_group.average_mark не может быть null" },
            semesterEnum = group.semesterEnum?.let { Semester.valueOf(it.literal) },
            groupAdmin = person?.let { PersonMapper.toDto(it) },
            ownerId = group.creatorId!!
        )
    }
}
