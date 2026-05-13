package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.RemoveByIdRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class RemoveByIdCommand(private val cm: ICollectionManager) : ServerCommand() {
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        request as RemoveByIdRequest
        cm.removeById(request.id, request.login, request.password)
        return CommandResponse(ExitCode.OK, "Элемент с id ${request.id} удалён")
    }
}
