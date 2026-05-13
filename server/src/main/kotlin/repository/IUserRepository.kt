package ru.qwuadrixx.repository

import org.jooq.DSLContext

interface IUserRepository {
    val dslContext: DSLContext

    fun register(login: String, password: String): Long
    fun login(login: String, password: String): Long
}