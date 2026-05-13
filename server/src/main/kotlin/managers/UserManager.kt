package ru.qwuadrixx.managers

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.qwuadrixx.repository.IUserRepository
import ru.qwuadrixx.utils.hashPassword

class UserManager : KoinComponent, IUserManager {

    private val userRepo: IUserRepository by inject()

    override fun register(login: String, password: String): Long =
        userRepo.register(login, hashPassword(password))

    override fun login(login: String, password: String): Long =
        userRepo.login(login, hashPassword(password))

    override fun verify(login: String, password: String) {
        userRepo.login(login, hashPassword(password))
    }
}
