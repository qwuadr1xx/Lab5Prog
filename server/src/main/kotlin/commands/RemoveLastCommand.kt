package ru.qwuadrixx.commands

import models.StudyGroup
import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class RemoveLastCommand(private val cm: ICollectionManager) : ServerCommand() {
    private var removedElement: StudyGroup? = null
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        if (cm.collection.isEmpty()) return CommandResponse(ExitCode.ERROR, "Коллекция пуста")
        removedElement = cm.collection.last()
        cm.removeLast()
        return CommandResponse(ExitCode.OK, "Последний элемент удалён")
    }

    override fun undo(): IResponse {
        removedElement?.let { cm.collection.add(it) }
        return CommandResponse(ExitCode.OK, "Удаление последнего элемента отменено")
    }
}
