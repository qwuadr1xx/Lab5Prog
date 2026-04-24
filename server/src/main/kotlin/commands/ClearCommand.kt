package ru.qwuadrixx.commands

import models.StudyGroup
import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class ClearCommand(private val cm: ICollectionManager) : ServerCommand() {
    private var snapshot: List<StudyGroup> = emptyList()
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        snapshot = cm.collection.toList()
        cm.clear()
        return CommandResponse(ExitCode.OK, "Коллекция очищена")
    }

    override fun undo(): IResponse {
        cm.collection.clear()
        cm.collection.addAll(snapshot)
        return CommandResponse(ExitCode.OK, "Очистка коллекции отменена")
    }
}
