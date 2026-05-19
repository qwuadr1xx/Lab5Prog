package ru.qwuadrixx.mapper

import models.Person
import ru.qwuadrixx.generated.tables.records.PersonRecord
import java.time.ZoneOffset
import java.util.Date

object PersonMapper {

    fun toDto(record: PersonRecord): Person = Person(
        name = requireNotNull(record.name) { "person.name не может быть null" },
        birthday = record.birthday?.let { Date.from(it.atStartOfDay().toInstant(ZoneOffset.UTC)) },
        height = record.height,
        passportID = record.passportid
    )
}
