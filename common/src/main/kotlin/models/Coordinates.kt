package models

import utils.ensure
import java.io.Serializable

/**
 * Дата-класс Coordinates
 * @author qwuadrixx
 */
data class Coordinates(
    val x: Long, //Поле не может быть null
    val y: Double //Максимальное значение поля: 572, Поле не может быть null
) : Serializable {

    init {
        ensure(y <= 572) { "Максимальное значение поля Y: 572" }
    }
}