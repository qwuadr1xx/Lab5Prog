package ru.qwuadrixx.repository

import models.Token
import org.jooq.DSLContext

interface ITokenRepository {
    val dslContext: DSLContext
    fun save(token: Token)
    fun deleteExpired()
}