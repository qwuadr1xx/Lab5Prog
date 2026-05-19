package ru.qwuadrixx.service

import models.Token
import ru.qwuadrixx.repository.ITokenRepository
import utils.TokenUtils
import java.util.UUID

class TokenService(
    private val tokenUtils: TokenUtils,
    private val tokenRepo: ITokenRepository
) {
    companion object {
        private const val TOKEN_TTL_MS = 30 * 60 * 1000L
    }

    fun createToken(userId: Long, login: String, isAdmin: Boolean): String {
        val now = System.currentTimeMillis()
        val token = Token(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            expiresAt = now + TOKEN_TTL_MS,
            userId = userId,
            login = login,
            isAdmin = isAdmin
        )
        tokenRepo.save(token)
        return tokenUtils.encode(token)
    }

    fun validate(encoded: String): Token {
        val token = tokenUtils.decode(encoded)
            ?: throw SecurityException("Недействительный токен")
        if (!tokenUtils.isValid(token))
            throw SecurityException("Токен истёк")
        return token
    }
}