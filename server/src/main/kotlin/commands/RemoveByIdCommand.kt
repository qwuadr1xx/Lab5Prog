package ru.qwuadrixx.commands

import models.StudyGroup
import net.requests.IRequest
import net.requests.RemoveByIdRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class RemoveByIdCommand(private val cm: ICollectionManager) : ServerCommand() {
    private var removedElement: StudyGroup? = null
    private var removedIndex: Int = -1
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        request as RemoveByIdRequest
        val index = cm.collection.indexOfFirst { it.id == request.id }
        if (index == -1) return CommandResponse(ExitCode.ERROR, "Элемент с id ${request.id} не найден")
        removedIndex = index
        removedElement = cm.collection[index]
        cm.removeById(request.id)
        return CommandResponse(ExitCode.OK, "Элемент с id ${request.id} удалён")
    }

    override fun undo(): IResponse {
        removedElement?.let { cm.collection.add(removedIndex, it) }
        return CommandResponse(ExitCode.OK, "Удаление элемента с id ${removedElement?.id} отменено")
    }
}
