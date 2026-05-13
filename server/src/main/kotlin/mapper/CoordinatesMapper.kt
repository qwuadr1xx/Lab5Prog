package ru.qwuadrixx.mapper

import models.Coordinates
import ru.qwuadrixx.generated.tables.records.CoordinatesRecord

object CoordinatesMapper {

    fun toDto(record: CoordinatesRecord): Coordinates = Coordinates(
        x = requireNotNull(record.x) { "coordinates.x не может быть null" },
        y = requireNotNull(record.y) { "coordinates.y не может быть null" }
    )
}
