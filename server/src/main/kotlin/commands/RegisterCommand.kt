package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.RegisterRequest
import net.responses.IResponse
import net.responses.RegisterResponse
import ru.qwuadrixx.managers.IUserManager
import utils.ExitCode

class RegisterCommand(private val userManager: IUserManager) : ServerCommand() {
    override fun execute(request: IRequest): IResponse {
        request as RegisterRequest
        userManager.register(request.login, request.password)
        return RegisterResponse(ExitCode.OK, "Регистрация успешна")
    }
}
