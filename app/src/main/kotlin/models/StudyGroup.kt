package ru.qwuadrixx.app.models

import ru.qwuadrixx.app.utils.ensure
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

/**
 * Дата-класс StudyGroup
 * @author qwuadrixx
 */
data class StudyGroup(
    val id: Int = nextId(), //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    val name: String, //Поле не может быть null, Строка не может быть пустой
    val coordinates: Coordinates, //Поле не может быть null
    val creationDate: LocalDateTime = LocalDateTime.now(), //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    val studentsCount: Long? = null, //Значение поля должно быть больше 0, Поле может быть null
    val expelledStudents: Int, //Значение поля должно быть больше 0
    val averageMark: Long, //Значение поля должно быть больше 0, Поле не может быть null
    val semesterEnum: Semester? = null, //Поле может быть null
    val groupAdmin: Person? = null  //Поле может быть null
) : Comparable<StudyGroup> {

    init {
        ensure(id > 0) { "id должен быть > 0" }
        ensure(name.isNotBlank()) { "Name должно быть не пустым" }
        ensure(studentsCount == null || studentsCount > 0) { "StudentsCount должен быть > 0 или null" }
        ensure(expelledStudents > 0) { "ExpelledStudents должен быть > 0" }
        ensure(averageMark > 0) { "AverageMark должен быть > 0" }
    }

    override fun compareTo(other: StudyGroup): Int {
        return compareValuesBy(
            this, other,
            { it.name },
            { it.averageMark },
            { it.expelledStudents }
        )
    }

    companion object {
        private val seq = AtomicInteger(0)

        /**
         * Метод для генерации уникального id среди любого экземпляра StudyGroup
         * @return Сгенерированный id
         */
        fun nextId(): Int = seq.incrementAndGet()

        /**
         * Синхронизирует следующий id с максимальным
         * @param maxExistingId
         */
        fun syncIdGenerator(maxExistingId: Int) {
            seq.updateAndGet { cur -> maxOf(cur, maxExistingId) }
        }

        /**
         * Метод, для декрементации id
         */
        fun decrementId() {
            seq.decrementAndGet()
        }

        /**
         * Метод, для инкрементации id
         */
        fun incrementId() {
            seq.incrementAndGet()
        }
    }
}