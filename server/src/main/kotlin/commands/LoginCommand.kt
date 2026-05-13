package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.LoginRequest
import net.responses.IResponse
import net.responses.LoginResponse
import ru.qwuadrixx.managers.IUserManager
import utils.ExitCode

class LoginCommand(private val userManager: IUserManager) : ServerCommand() {
    override fun execute(request: IRequest): IResponse {
        request as LoginRequest
        userManager.login(request.login, request.password)
        return LoginResponse(ExitCode.OK, "Авторизация успешна")
    }
}
