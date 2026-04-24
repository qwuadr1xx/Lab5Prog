package ru.qwuadrixx.commands

import ru.qwuadrixx.utils.StudyGroupFactory
import net.requests.AddIfMaxRequest
import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class AddIfMaxCommand(private val cm: ICollectionManager) : ServerCommand() {
    private var addedId: Int = -1
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        request as AddIfMaxRequest
        val group = StudyGroupFactory.assignServerFields(request.studyGroup)
        val added = cm.addIfMax(group)
        return if (added) {
            addedId = group.id
            CommandResponse(ExitCode.OK, "Элемент с id $addedId добавлен в коллекцию")
        } else {
            StudyGroupFactory.decrementId()
            CommandResponse(ExitCode.OK, "Элемент не добавлен: не превышает максимальный элемент коллекции")
        }
    }

    override fun undo(): IResponse {
        if (addedId == -1) return CommandResponse(ExitCode.OK, "Элемент не был добавлен — отмена не требуется")
        val index = cm.collection.indexOfFirst { it.id == addedId }
        if (index != -1) {
            cm.collection.removeAt(index)
            StudyGroupFactory.decrementId()
        }
        return CommandResponse(ExitCode.OK, "Добавление элемента с id $addedId отменено")
    }
}
