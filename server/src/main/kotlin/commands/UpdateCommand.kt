package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.UpdateRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class UpdateCommand(private val cm: ICollectionManager) : ServerCommand() {
    override val isUndoable = true

    override fun execute(request: IRequest, userId: Long): IResponse {
        request as UpdateRequest
        cm.updateById(request.id, request.studyGroup, userId)
        return CommandResponse(ExitCode.OK, "Элемент с id ${request.id} обновлён")
    }
}