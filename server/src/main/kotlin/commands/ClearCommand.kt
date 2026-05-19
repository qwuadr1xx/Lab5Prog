package ru.qwuadrixx.commands

import net.requests.ClearRequest
import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class ClearCommand(private val cm: ICollectionManager) : ServerCommand() {
    override val isUndoable = true

    override fun execute(request: IRequest, userId: Long): IResponse {
        request as ClearRequest
        cm.clear(userId)
        return CommandResponse(ExitCode.OK, "Коллекция очищена")
    }
}