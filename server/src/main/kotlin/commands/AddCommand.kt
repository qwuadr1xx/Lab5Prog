package ru.qwuadrixx.commands

import net.requests.AddRequest
import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import ru.qwuadrixx.utils.StudyGroupFactory
import utils.ExitCode

class AddCommand(private val cm: ICollectionManager) : ServerCommand() {
    private var addedId: Int = -1
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        request as AddRequest
        val group = StudyGroupFactory.assignServerFields(request.studyGroup)
        cm.add(group)
        addedId = group.id
        return CommandResponse(ExitCode.OK, "Элемент с id $addedId добавлен")
    }

    override fun undo(): IResponse {
        val index = cm.collection.indexOfFirst { it.id == addedId }
        if (index == -1) return CommandResponse(ExitCode.ERROR, "Элемент с id $addedId не найден при отмене")
        cm.collection.removeAt(index)
        StudyGroupFactory.decrementId()
        return CommandResponse(ExitCode.OK, "Добавление элемента с id $addedId отменено")
    }
}
