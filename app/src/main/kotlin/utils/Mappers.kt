package ru.qwuadrixx.app.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

fun stringToDate(date: String): Date {
    val formatter = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm")
    val ldt = LocalDateTime.parse(date.trim(), formatter)
    return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())
}