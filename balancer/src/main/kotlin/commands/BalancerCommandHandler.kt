package ru.qwuadrixx.balancer.commands

import models.ServerNodeInfo
import net.requests.*
import net.responses.*
import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.manager.INodeManager
import ru.qwuadrixx.balancer.process.IProcessManager
import utils.CommandName
import utils.ExitCode
import utils.ServerFilterArg
import utils.TokenUtils
import java.net.DatagramSocket

class BalancerCommandHandler(
    private val nodeManager: INodeManager,
    private val processManager: IProcessManager,
    private val tokenUtils: TokenUtils
) {

    private val log = LoggerFactory.getLogger(BalancerCommandHandler::class.java)

    companion object {
        val BALANCER_COMMANDS: Set<CommandName> = setOf(
            CommandName.NODES,
            CommandName.LIST_SERVERS,
            CommandName.ENABLE_SERVER,
            CommandName.DISABLE_SERVER,
            CommandName.ADD_SERVER,
            CommandName.REMOVE_SERVER
        )
    }

    fun handles(commandName: CommandName): Boolean = commandName in BALANCER_COMMANDS

    fun execute(request: IRequest): IResponse {
        val token = tokenUtils.decode(request.token)
        if (token == null || !token.isAdmin || !tokenUtils.isValid(token)) {
            log.warn("Отказано в доступе к команде {}: требуются права администратора", request.commandName)
            return CommandResponse(ExitCode.ERROR, "Недостаточно прав: команда ${request.commandName} доступна только администраторам")
        }
        return when (request.commandName) {
            CommandName.NODES -> handleNodes()
            CommandName.LIST_SERVERS -> handleListServers(request as ListServersRequest)
            CommandName.ENABLE_SERVER -> handleEnableServer(request as EnableServerRequest)
            CommandName.DISABLE_SERVER -> handleDisableServer(request as DisableServerRequest)
            CommandName.ADD_SERVER -> handleAddServer(request as AddServerRequest)
            CommandName.REMOVE_SERVER -> handleRemoveServer(request as RemoveServerRequest)
            else -> CommandResponse(ExitCode.ERROR, "Неизвестная команда балансировщика: ${request.commandName}")
        }
    }

    private fun handleNodes(): IResponse {
        val nodes = nodeManager.nodes()
        if (nodes.isEmpty()) return CommandResponse(ExitCode.OK, "Нет зарегистрированных узлов")
        val message = nodes.joinToString("\n") { "[${it.id}] ${it.address.hostString}:${it.address.port}" }
        return CommandResponse(ExitCode.OK, message)
    }

    private fun handleListServers(request: ListServersRequest): IResponse {
        val filtered = nodeManager.nodes().filter { node ->
            when (request.filter) {
                ServerFilterArg.ALL -> true
                ServerFilterArg.ENABLED -> node.isEnabled
                ServerFilterArg.DISABLED -> !node.isEnabled
                ServerFilterArg.AVAILABLE -> node.isEnabled && node.isAvailable()
                ServerFilterArg.UNAVAILABLE -> !node.isEnabled || !node.isAvailable()
            }
        }
        val infos = filtered.map { node ->
            ServerNodeInfo(
                id = node.id,
                host = node.address.hostString,
                port = node.address.port,
                isEnabled = node.isEnabled,
                isAvailable = node.isAvailable(),
                isMain = node.isMain,
                createdAt = node.createdAt.toEpochMilli(),
                notAvailableSince = node.notAvailableSince?.toEpochMilli()
            )
        }
        return ListServersResponse(ExitCode.OK, infos)
    }

    private fun handleEnableServer(request: EnableServerRequest): IResponse {
        val node = nodeManager.findById(request.serverId)
            ?: return EnableServerResponse(ExitCode.ERROR, "Сервер с id=${request.serverId} не найден")
        node.enable()
        return EnableServerResponse(ExitCode.OK, "Сервер ${node.address.hostString}:${node.address.port} включён")
    }

    private fun handleDisableServer(request: DisableServerRequest): IResponse {
        val node = nodeManager.findById(request.serverId)
            ?: return DisableServerResponse(ExitCode.ERROR, "Сервер с id=${request.serverId} не найден")
        node.disable()
        return DisableServerResponse(ExitCode.OK, "Сервер ${node.address.hostString}:${node.address.port} выключен")
    }

    private fun handleAddServer(request: AddServerRequest): IResponse {
        var newPort: Int = request.port
        val id = try {
            val ds = DatagramSocket(request.port)
            newPort = ds.localPort
            ds.close()
            processManager.addServer(newPort)
        } catch (e: Exception) {
            return AddServerResponse(ExitCode.ERROR, "Ошибка запуска сервера на порту $newPort: ${e.message}", -1)
        }
        return AddServerResponse(ExitCode.OK, "Сервер localhost:${newPort} запущен (id=$id)", id)
    }

    private fun handleRemoveServer(request: RemoveServerRequest): IResponse {
        val node = nodeManager.findById(request.serverId)
            ?: return RemoveServerResponse(ExitCode.ERROR, "Сервер с id=${request.serverId} не найден")
        if (node.isMain) return RemoveServerResponse(ExitCode.ERROR, "Нельзя удалить основной сервер (id=${node.id})")
        nodeManager.remove(request.serverId)
        try {
            processManager.removeServer(request.serverId)
        } catch (e: NoSuchElementException) {
            log.warn("Процесс для id={} не управляется ProcessManager (статический сервер?): {}", request.serverId, e.message)
        } catch (e: Exception) {
            log.error("Ошибка остановки процесса id={}: {}", request.serverId, e.message)
        }
        return RemoveServerResponse(ExitCode.OK, "Сервер ${node.address.hostString}:${node.address.port} удалён")
    }
}