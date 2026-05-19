package ru.qwuadrixx.app.commands

import net.requests.ListServersRequest
import net.responses.CommandResponse
import net.responses.EnableServerResponse
import net.responses.ListServersResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode
import utils.ServerFilterArg
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ListServers(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "list_servers", description = "Показать список серверов [ALL|ENABLED|DISABLED|AVAILABLE|UNAVAILABLE]") {

    private val fmt = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy").withZone(ZoneId.systemDefault())

    override fun execute(): ExitCode {
        val filter = readFilter()
        try {
            var response = rudpClient.sendAndReceive(ListServersRequest(filter, session.token))
            if (response is CommandResponse) {
                console.printLine(response.message)
                return ExitCode.ERROR
            } else {
                response = response as ListServersResponse
            }
            if (response.servers.isEmpty()) {
                console.printLine("Нет серверов по фильтру $filter")
            } else {
                response.servers.forEach { s ->
                    val available = if (s.isAvailable) "доступен" else "недоступен (с ${fmt.format(Instant.ofEpochMilli(s.notAvailableSince!!))})"
                    val enabled = if (s.isEnabled) "включён" else "выключен"
                    val main = if (s.isMain) " [MAIN]" else ""
                    console.printLine("[${s.id}]$main ${s.host}:${s.port} | $enabled | $available | добавлен ${fmt.format(Instant.ofEpochMilli(s.createdAt))}")
                }
            }
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }

    private fun readFilter(): ServerFilterArg {
        val names = ServerFilterArg.entries.joinToString("|") { it.name }
        while (true) {
            console.printLine("Фильтр [$names] (Enter — ALL):")
            val input = console.readLine().trim()
            if (input.isEmpty()) return ServerFilterArg.ALL
            val match = ServerFilterArg.entries.find { it.name.equals(input, ignoreCase = true) }
            if (match != null) return match
            console.printLine("Неверный фильтр. Допустимые значения: $names")
        }
    }
}