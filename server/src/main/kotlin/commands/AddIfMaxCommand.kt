package ru.qwuadrixx.commands

import net.requests.AddIfMaxRequest
import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class AddIfMaxCommand(private val cm: ICollectionManager) : ServerCommand() {
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        request as AddIfMaxRequest
        val added = cm.addIfMax(request.studyGroup, request.login, request.password)
        return if (added)
            CommandResponse(ExitCode.OK, "Элемент добавлен в коллекцию")
        else
            CommandResponse(ExitCode.OK, "Элемент не добавлен: не превышает максимальный элемент коллекции")
    }
}
