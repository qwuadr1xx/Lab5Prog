package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.RegisterRequest
import net.responses.IResponse
import net.responses.RegisterResponse
import ru.qwuadrixx.managers.IUserManager
import ru.qwuadrixx.service.TokenService
import utils.ExitCode

class RegisterCommand(
    private val userManager: IUserManager,
    private val tokenService: TokenService
) : ServerCommand() {
    override fun execute(request: IRequest, userId: Long): IResponse {
        request as RegisterRequest
        val newUserId = userManager.register(request.login, request.password)
        val token = tokenService.createToken(newUserId, request.login, false)
        return RegisterResponse(ExitCode.OK, "Регистрация успешна", token)
    }
}