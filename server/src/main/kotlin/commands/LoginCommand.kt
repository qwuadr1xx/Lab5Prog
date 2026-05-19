package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.LoginRequest
import net.responses.IResponse
import net.responses.LoginResponse
import ru.qwuadrixx.managers.IUserManager
import ru.qwuadrixx.service.TokenService
import utils.ExitCode

class LoginCommand(
    private val userManager: IUserManager,
    private val tokenService: TokenService
) : ServerCommand() {
    override fun execute(request: IRequest, userId: Long): IResponse {
        request as LoginRequest
        val userInfo = userManager.login(request.login, request.password)
        val token = tokenService.createToken(userInfo.userId, userInfo.login, userInfo.isAdmin)
        return LoginResponse(ExitCode.OK, "Авторизация успешна", token)
    }
}