package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.InsertAtRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class InsertAtCommand(private val cm: ICollectionManager) : ServerCommand() {
    override val isUndoable = true

    override fun execute(request: IRequest, userId: Long): IResponse {
        request as InsertAtRequest
        cm.insertAt(request.index, request.studyGroup, userId)
        return CommandResponse(ExitCode.OK, "Элемент добавлен на позицию ${request.index}")
    }
}