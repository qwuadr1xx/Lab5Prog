package ru.qwuadrixx.managers

import exception.ExistingLoginException
import exception.NotFoundException
import net.requests.*
import net.responses.CommandResponse
import net.responses.IResponse
import org.jooq.exception.DataAccessException
import org.slf4j.LoggerFactory
import ru.qwuadrixx.commands.*
import ru.qwuadrixx.parsers.LineReader
import ru.qwuadrixx.parsers.StudyGroupParser
import ru.qwuadrixx.repository.IHistoryRepository
import ru.qwuadrixx.service.TokenService
import utils.CommandName
import utils.ExitCode

private val NO_AUTH_COMMANDS = setOf(CommandName.LOGIN, CommandName.REGISTER)

class RequestManager(
    private val cm: ICollectionManager,
    private val historyRepo: IHistoryRepository,
    private val userManager: IUserManager,
    private val tokenService: TokenService
) : IRequestManager {

    private val logger = LoggerFactory.getLogger(RequestManager::class.java)
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
        CommandName.UNDO to { UndoCommand(historyRepo, cm) },
        CommandName.LOGIN to { LoginCommand(userManager, tokenService) },
        CommandName.REGISTER to { RegisterCommand(userManager, tokenService) }
    )

    override fun dispatch(request: IRequest): IResponse =
        if (request is ExecuteScriptRequest) executeScript(request)
        else executeSingleCommand(request)

    private fun executeSingleCommand(request: IRequest): IResponse {
        logger.info("Выполняется команда: {}", request.commandName)
        return try {
            val userId: Long = if (request.commandName !in NO_AUTH_COMMANDS) {
                tokenService.validate(request.token).userId
            } else {
                -1L
            }
            val factory = commandFactories[request.commandName]
                ?: return CommandResponse(ExitCode.ERROR, "Неизвестная команда: ${request.commandName}").also {
                    logger.warn("Неизвестная команда: {}", request.commandName)
                }
            val command = factory()
            val snapshotBefore = if (command.isUndoable && !cm.scriptMode) cm.takeSnapshot() else null
            val response = command.execute(request, userId)
            if (snapshotBefore != null && response.exitCode == ExitCode.OK) {
                historyRepo.push(snapshotBefore, userId)
            }
            logger.info("Команда {} завершена со статусом {}", request.commandName, response.exitCode)
            response
        } catch (e: SecurityException) {
            logger.warn("Ошибка авторизации для команды {}: {}", request.commandName, e.message)
            CommandResponse(ExitCode.ERROR, "Ошибка авторизации: ${e.message}")
        } catch (e: NoSuchElementException) {
            logger.warn("Элемент не найден при выполнении {}: {}", request.commandName, e.message)
            CommandResponse(ExitCode.ERROR, "Не найдено: ${e.message}")
        } catch (e: NotFoundException) {
            logger.warn("Элемент не найден при выполнении {}: {}", request.commandName, e.message)
            CommandResponse(ExitCode.ERROR, "Не найдено: ${e.message}")
        } catch (e: ExistingLoginException) {
            logger.warn("Конфликт при выполнении {}: {}", request.commandName, e.message)
            CommandResponse(ExitCode.ERROR, e.message ?: "Пользователь уже существует")
        } catch (e: DataAccessException) {
            val cause = e.cause
            logger.warn("Ошибка БД при выполнении {}: {}", request.commandName, cause?.message ?: e.message)
            when (cause) {
                is SecurityException -> CommandResponse(ExitCode.ERROR, "Ошибка авторизации: ${cause.message}")
                is NoSuchElementException -> CommandResponse(ExitCode.ERROR, "Не найдено: ${cause.message}")
                is NotFoundException -> CommandResponse(ExitCode.ERROR, "Не найдено: ${cause.message}")
                is ExistingLoginException -> CommandResponse(ExitCode.ERROR, cause.message ?: "Пользователь уже существует")
                else -> CommandResponse(ExitCode.ERROR, cause?.message ?: e.message ?: "Ошибка базы данных")
            }
        } catch (e: Exception) {
            logger.error("Ошибка при выполнении команды {}: {}", request.commandName, e.message, e)
            CommandResponse(ExitCode.ERROR, "Ошибка: ${e.message}")
        }
    }

    private fun executeScript(request: ExecuteScriptRequest): IResponse {
        val snapshot = cm.takeSnapshot()
        cm.scriptMode = true
        logger.info("Начало выполнения скрипта с отложенной записью ({} строк)", request.lines.size)
        val results = mutableListOf<String>()

        try {
            val userId = tokenService.validate(request.token).userId

            val reader = LineReader(request.lines)
            while (reader.hasNext()) {
                val commandName = reader.readLine()
                if (commandName.isEmpty()) continue

                val scriptRequest = try {
                    parseScriptCommand(commandName, reader, request.token)
                } catch (e: Exception) {
                    logger.warn("Ошибка разбора команды '{}': {}", commandName, e.message)
                    cm.scriptMode = false
                    cm.restoreSnapshot(snapshot)
                    return CommandResponse(ExitCode.ERROR, "Скрипт откатан: ошибка разбора '$commandName': ${e.message}")
                } ?: continue

                val response = executeSingleCommand(scriptRequest)
                if (response.exitCode == ExitCode.ERROR) {
                    logger.warn("Скрипт откатан: ошибка в команде {}", scriptRequest.commandName)
                    cm.scriptMode = false
                    cm.restoreSnapshot(snapshot)
                    return CommandResponse(ExitCode.ERROR, "Скрипт прерван и откатан: ${(response as CommandResponse).message}")
                }
                (response as? CommandResponse)?.message?.takeIf { it.isNotEmpty() }?.let { results.add(it) }
            }

            cm.scriptMode = false
            cm.applyScriptDiff(snapshot, userId)
            historyRepo.push(snapshot, userId)
            logger.info("Скрипт выполнен успешно, изменения записаны в БД")
            return CommandResponse(ExitCode.OK, results.joinToString("\n"))
        } catch (e: Exception) {
            logger.error("Необработанная ошибка в скрипте: {}", e.message, e)
            cm.scriptMode = false
            cm.restoreSnapshot(snapshot)
            return CommandResponse(ExitCode.ERROR, "Скрипт прерван: ${e.message}")
        }
    }

    private fun parseScriptCommand(commandName: String, reader: LineReader, token: String): IRequest? =
        when (commandName) {
            "add" -> AddRequest(StudyGroupParser.parse(reader), token)
            "add_if_max" -> AddIfMaxRequest(StudyGroupParser.parse(reader), token)
            "show" -> ShowRequest(token)
            "info" -> InfoRequest(token)
            "clear" -> ClearRequest(token)
            "remove_last" -> RemoveLastRequest(token)
            "average_of_average_mark" -> AverageOfAverageMarkRequest(token)
            "remove_by_id" -> RemoveByIdRequest(reader.readLine().toInt(), token)
            "insert_at" -> InsertAtRequest(reader.readLine().toInt(), StudyGroupParser.parse(reader), token)
            "update" -> {
                val id = reader.readLine().toInt()
                UpdateRequest(id, StudyGroupParser.parse(reader, id), token)
            }
            "count_greater_than_average_mark" -> CountGreaterThanAverageMarkRequest(reader.readLine().toLong(), token)
            "count_less_than_average_mark" -> CountLessThanAverageMarkRequest(reader.readLine().toLong(), token)
            "undo" -> UndoRequest(reader.readLine().toInt(), token)
            else -> null
        }
}