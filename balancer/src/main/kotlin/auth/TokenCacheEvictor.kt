package ru.qwuadrixx.balancer.auth

import org.slf4j.LoggerFactory

class TokenCacheEvictor(private val cache: AdminTokenCache) : Runnable {

    private val log = LoggerFactory.getLogger(TokenCacheEvictor::class.java)

    override fun run() {
        log.info("TokenCacheEvictor запущен")
        while (!Thread.currentThread().isInterrupted) {
            try {
                Thread.sleep(60_000)
                cache.evictExpired()
                log.debug("Истёкшие токены удалены из кеша")
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        log.info("TokenCacheEvictor остановлен")
    }
}
