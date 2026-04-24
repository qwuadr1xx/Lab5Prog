package ru.qwuadrixx.commands

import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.CollectionManager
import ru.qwuadrixx.managers.FileManager
import utils.ExitCode

class SaveCommand(private val cm: CollectionManager, private val fm: FileManager) : ServerCommand() {
    override fun execute(request: IRequest): IResponse {
        fm.writeCollection(cm.collection)
        return CommandResponse(ExitCode.OK, "Коллекция сохранена в файл")
    }
}
