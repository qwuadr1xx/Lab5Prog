package ru.qwuadrixx.balancer.auth

import models.Token
import java.util.concurrent.ConcurrentHashMap

class AdminTokenCache {
    private val cache = ConcurrentHashMap<String, Token>()

    fun put(token: Token) {
        cache[token.id] = token
    }

    fun evictExpired() {
        val now = System.currentTimeMillis()
        cache.values.removeIf { it.expiresAt < now }
    }

    fun isAdmin(tokenId: String): Boolean = cache[tokenId]?.isAdmin == true

    fun all(): Collection<Token> = cache.values
}