package ru.qwuadrixx.commands

import models.StudyGroup
import net.requests.IRequest
import net.requests.UpdateRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import ru.qwuadrixx.utils.StudyGroupFactory
import utils.ExitCode

class UpdateCommand(private val cm: ICollectionManager) : ServerCommand() {
    private var oldElement: StudyGroup? = null
    private var oldIndex: Int = -1
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        request as UpdateRequest
        val index = cm.collection.indexOfFirst { it.id == request.id }
        if (index == -1) return CommandResponse(ExitCode.ERROR, "Элемент с id ${request.id} не найден")
        oldIndex = index
        oldElement = cm.collection[index]
        val updated = StudyGroupFactory.forUpdate(request.studyGroup, request.id, oldElement!!.creationDate)
        cm.updateById(request.id, updated)
        return CommandResponse(ExitCode.OK, "Элемент с id ${request.id} обновлён")
    }

    override fun undo(): IResponse {
        oldElement?.let {
            cm.collection.removeAt(oldIndex)
            cm.collection.add(oldIndex, it)
        }
        return CommandResponse(ExitCode.OK, "Обновление элемента с id ${oldElement?.id} отменено")
    }
}
