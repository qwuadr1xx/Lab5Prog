package ru.qwuadrixx.commands

import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class RemoveLastCommand(private val cm: ICollectionManager) : ServerCommand() {
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        request as net.requests.RemoveLastRequest
        cm.removeLast(request.login, request.password)
        return CommandResponse(ExitCode.OK, "Последний элемент удалён")
    }
}
