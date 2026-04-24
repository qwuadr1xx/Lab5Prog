package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.InsertAtRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import ru.qwuadrixx.utils.StudyGroupFactory
import utils.ExitCode

class InsertAtCommand(private val cm: ICollectionManager) : ServerCommand() {
    private var insertedIndex: Int = -1
    override val isUndoable = true

    override fun execute(request: IRequest): IResponse {
        request as InsertAtRequest
        cm.insertAt(request.index, StudyGroupFactory.assignServerFields(request.studyGroup))
        insertedIndex = request.index
        return CommandResponse(ExitCode.OK, "Элемент добавлен на позицию ${request.index}")
    }

    override fun undo(): IResponse {
        cm.collection.removeAt(insertedIndex)
        StudyGroupFactory.decrementId()
        return CommandResponse(ExitCode.OK, "Вставка на позицию $insertedIndex отменена")
    }
}
