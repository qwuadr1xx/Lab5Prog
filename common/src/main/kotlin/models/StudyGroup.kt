package models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import utils.ensure

/**
 * Дата-класс StudyGroup
 * @author qwuadrixx
 */
@Serializable
data class StudyGroup(
    val id: Int = 0,
    val name: String,
    val coordinates: Coordinates,
    val creationDate: LocalDateTime = LocalDateTime(1970, 1, 1, 0, 0),
    val studentsCount: Long? = null,
    val expelledStudents: Int,
    val averageMark: Long,
    val semesterEnum: Semester? = null,
    val groupAdmin: Person? = null,
    val ownerId: Long?
) : Comparable<StudyGroup> {

    init {
        ensure(name.isNotBlank()) { "Name должно быть не пустым" }
        ensure(studentsCount == null || studentsCount > 0) { "StudentsCount должен быть > 0 или null" }
        ensure(expelledStudents > 0) { "ExpelledStudents должен быть > 0" }
        ensure(averageMark > 0) { "AverageMark должен быть > 0" }
    }

    override fun compareTo(other: StudyGroup): Int =
        compareValuesBy(this, other, { it.name }, { it.averageMark }, { it.expelledStudents })
}
