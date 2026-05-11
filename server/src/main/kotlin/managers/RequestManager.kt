package ru.qwuadrixx.managers

import net.requests.*
import net.responses.CommandResponse
import net.responses.IResponse
import org.slf4j.LoggerFactory
import ru.qwuadrixx.commands.*
import ru.qwuadrixx.parsers.LineReader
import ru.qwuadrixx.parsers.StudyGroupParser
import utils.CommandName
import utils.ExitCode

class RequestManager(
    private val cm: ICollectionManager
) : IRequestManager {

    private val logger = LoggerFactory.getLogger(RequestManager::class.java)
    private val history: ArrayDeque<ServerCommand> = ArrayDeque()
    private val commandFactories: Map<CommandName, () -> ServerCommand> = mapOf(
        CommandName.ADD to { AddCommand(cm) },
        CommandName.ADD_IF_MAX to { AddIfMaxCommand(cm) },
        CommandName.SHOW to { ShowCommand(cm) },
        CommandName.INFO to { InfoCommand(cm) },
        CommandName.CLEAR to { ClearCommand(cm) },
        CommandName.REMOVE_BY_ID to { RemoveByIdCommand(cm) },
        CommandName.REMOVE_LAST to { RemoveLastCommand(cm) },
        CommandName.INSERT_AT to { InsertAtCommand(cm) },
        CommandName.UPDATE to { UpdateCommand(cm) },
        CommandName.AVERAGE_OF_AVERAGE_MARK to { AverageOfAverageMarkCommand(cm) },
        CommandName.COUNT_GREATER_THAN_AVERAGE_MARK to { CountGreaterThanAverageMarkCommand(cm) },
        CommandName.COUNT_LESS_THAN_AVERAGE_MARK to { CountLessThanAverageMarkCommand(cm) },
        CommandName.UNDO to { UndoCommand(history) }
    )

    override fun dispatch(request: IRequest): IResponse =
        if (request is ExecuteScriptRequest) executeScript(request.lines)
        else executeSingleCommand(request)

    private fun executeSingleCommand(request: IRequest): IResponse {
        logger.info("Выполняется команда: {}", request.commandName)
        return try {
            val factory = commandFactories[request.commandName]
                ?: return CommandResponse(ExitCode.ERROR, "Неизвестная команда: ${request.commandName}").also {
                    logger.warn("Неизвестная команда: {}", request.commandName)
                }
            val command = factory()
            val response = command.execute(request)
            if (response.exitCode == ExitCode.OK && command.isUndoable) {
                history.addLast(command)
            }
            logger.info("Команда {} завершена со статусом {}", request.commandName, response.exitCode)
            response
        } catch (e: Exception) {
            logger.error("Ошибка при выполнении команды {}: {}", request.commandName, e.message, e)
            CommandResponse(ExitCode.ERROR, "Ошибка: ${e.message}")
        }
    }

    private fun executeScript(lines: List<String>): IResponse {
        val collectionSnapshot = cm.takeSnapshot()
        val historySnapshot = history.toList()

        logger.info("Начало транзакционного выполнения скрипта ({} строк)", lines.size)
        val results = mutableListOf<String>()

        val reader = LineReader(lines)
        while (reader.hasNext()) {
            val commandName = reader.readLine()
            if (commandName.isEmpty()) continue

            val request = try {
                parseScriptCommand(commandName, reader)
            } catch (e: Exception) {
                logger.warn("Ошибка разбора команды '{}': {}", commandName, e.message)
                cm.restoreSnapshot(collectionSnapshot)
                history.clear()
                history.addAll(historySnapshot)
                return CommandResponse(
                    ExitCode.ERROR,
                    "Скрипт откатан: ошибка разбора команды '$commandName': ${e.message}"
                )
            } ?: continue

            val response = executeSingleCommand(request)
            if (response.exitCode == ExitCode.ERROR) {
                cm.restoreSnapshot(collectionSnapshot)
                history.clear()
                history.addAll(historySnapshot)
                logger.warn("Скрипт откатан: ошибка в команде {}", request.commandName)
                return CommandResponse(
                    ExitCode.ERROR,
                    "Скрипт прерван и откатан: ${(response as CommandResponse).message}"
                )
            }
            (response as? CommandResponse)?.message?.takeIf { it.isNotEmpty() }?.let { results.add(it) }
        }

        logger.info("Скрипт выполнен успешно")
        return CommandResponse(ExitCode.OK, results.joinToString("\n"))
    }

    private fun parseScriptCommand(commandName: String, reader: LineReader): IRequest? = when (commandName) {
        "add" -> AddRequest(StudyGroupParser.parse(reader))
        "add_if_max" -> AddIfMaxRequest(StudyGroupParser.parse(reader))
        "show" -> ShowRequest()
        "info" -> InfoRequest()
        "clear" -> ClearRequest()
        "remove_last" -> RemoveLastRequest()
        "average_of_average_mark" -> AverageOfAverageMarkRequest()
        "remove_by_id" -> RemoveByIdRequest(reader.readLine().toInt())
        "insert_at" -> InsertAtRequest(reader.readLine().toInt(), StudyGroupParser.parse(reader))
        "update" -> {
            val id = reader.readLine().toInt(); UpdateRequest(id, StudyGroupParser.parse(reader, id))
        }
        "count_greater_than_average_mark" -> CountGreaterThanAverageMarkRequest(reader.readLine().toLong())
        "count_less_than_average_mark" -> CountLessThanAverageMarkRequest(reader.readLine().toLong())
        "undo" -> UndoRequest(reader.readLine().toInt())
        else -> null
    }
}
