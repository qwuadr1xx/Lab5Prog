package ru.qwuadrixx.repository

import exception.ExistingLoginException
import exception.NotFoundException
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.qwuadrixx.generated.tables.references.USERS

class UserRepository : KoinComponent, IUserRepository {
    override val dslContext: DSLContext by inject()

    override fun register(login: String, password: String): Long =
        dslContext.transactionResult { config ->
            val ctx = DSL.using(config)

            return@transactionResult ctx.insertInto(USERS)
                .set(USERS.LOGIN, login)
                .set(USERS.PASSWORD_HASH, password)
                .returning(USERS.ID)
                .fetchOptional { it.get(USERS.ID) }
                .orElseThrow { ExistingLoginException("Пользователь $login уже зарегестрирован") }
        }

    override fun login(login: String, password: String): Long =
        dslContext.transactionResult { config ->
            val ctx = DSL.using(config)

            return@transactionResult ctx.select(USERS.ID)
                .from(USERS)
                .where(USERS.LOGIN.eq(login).and(USERS.PASSWORD_HASH.eq(password)))
                .fetchOptional { it.get(USERS.ID) }
                .orElseThrow { NotFoundException("Пользователь $login не найден.") }
        }
}
