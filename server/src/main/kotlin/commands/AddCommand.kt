package ru.qwuadrixx.commands

import net.requests.AddRequest
import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class AddCommand(private val cm: ICollectionManager) : ServerCommand() {
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        request as AddRequest
        cm.add(request.studyGroup, request.login, request.password)
        return CommandResponse(ExitCode.OK, "Элемент добавлен")
    }
}
