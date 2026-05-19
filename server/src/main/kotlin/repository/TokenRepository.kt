package ru.qwuadrixx.repository

import models.Token
import org.jooq.DSLContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.qwuadrixx.generated.tables.references.TOKENS
import java.time.LocalDateTime
import java.time.ZoneOffset

class TokenRepository : KoinComponent, ITokenRepository {

    override val dslContext: DSLContext by inject()

    override fun save(token: Token) {
        dslContext.insertInto(TOKENS)
            .set(TOKENS.ID, token.id)
            .set(TOKENS.USER_ID, token.userId)
            .set(TOKENS.LOGIN, token.login)
            .set(TOKENS.IS_ADMIN, token.isAdmin)
            .set(TOKENS.CREATED_AT, LocalDateTime.ofEpochSecond(token.createdAt / 1000, ((token.createdAt % 1000) * 1_000_000).toInt(), ZoneOffset.UTC))
            .set(TOKENS.EXPIRES_AT, LocalDateTime.ofEpochSecond(token.expiresAt / 1000, ((token.expiresAt % 1000) * 1_000_000).toInt(), ZoneOffset.UTC))
            .execute()
    }

    override fun deleteExpired() {
        dslContext.deleteFrom(TOKENS)
            .where(TOKENS.EXPIRES_AT.lt(LocalDateTime.now(ZoneOffset.UTC)))
            .execute()
    }
}