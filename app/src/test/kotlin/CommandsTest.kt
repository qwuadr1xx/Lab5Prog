import net.requests.AddRequest
import net.requests.AverageOfAverageMarkRequest
import net.requests.ClearRequest
import net.requests.CountGreaterThanAverageMarkRequest
import net.requests.CountLessThanAverageMarkRequest
import net.requests.InfoRequest
import net.requests.RemoveByIdRequest
import net.requests.RemoveLastRequest
import net.requests.ShowRequest
import net.requests.UndoRequest
import net.responses.CommandResponse
import net.responses.IResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.commands.*
import ru.qwuadrixx.app.managers.CommandManager
import ru.qwuadrixx.app.managers.ICommandManager
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode
import java.io.BufferedReader
import java.io.InputStreamReader

class MockRUDPClient(private val response: IResponse) : IRUDPClient {
    var lastRequest: net.requests.IRequest? = null
    override fun sendAndReceive(request: net.requests.IRequest): IResponse {
        lastRequest = request
        return response
    }
}

internal class CommandsTest {
    private lateinit var console: TestConsole
    private lateinit var commandManager: ICommandManager
    private lateinit var mockClient: MockRUDPClient

    @BeforeEach
    fun setUp() {
        console = TestConsole()
        mockClient = MockRUDPClient(CommandResponse(ExitCode.OK, ""))
        commandManager = CommandManager()
        val session = UserSession()

        commandManager.apply {
            register(Add(mockClient, console, session))
            register(AddIfMax(mockClient, console, session))
            register(Show(mockClient, console, session))
            register(AverageOfAverageMark(mockClient, console, session))
            register(Clear(mockClient, console, session))
            register(CountLessThanAverageMark(mockClient, console, session))
            register(CountGreaterThanAverageMark(mockClient, console, session))
            register(ExecuteScript(mockClient, console, session))
            register(Exit(console))
            register(Help(console, this))
            register(Info(mockClient, console, session))
            register(InsertAt(mockClient, console, session))
            register(RemoveById(mockClient, console, session))
            register(RemoveLast(mockClient, console, session))
            register(Update(mockClient, console, session))
            register(Undo(mockClient, console, session))
        }
    }

    @Test
    fun add_should_send_add_request_and_return_ok() {
        console.reader = BufferedReader(
            InputStreamReader(
                """
                TestGroup
                1
                2
                10
                1
                5
                FIRST
                0
                """.trimIndent().byteInputStream()
            )
        )

        val exitCode = commandManager.getCommand("add").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is AddRequest)
    }

    @Test
    fun show_should_send_show_request_and_return_ok() {
        mockClient = MockRUDPClient(CommandResponse(ExitCode.OK, "Group1\nGroup2"))
        commandManager = CommandManager()
        commandManager.register(Show(mockClient, console, UserSession()))

        val exitCode = commandManager.getCommand("show").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is ShowRequest)
    }

    @Test
    fun clear_should_send_clear_request_and_return_ok() {
        val exitCode = commandManager.getCommand("clear").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is ClearRequest)
    }

    @Test
    fun info_should_send_info_request_and_return_ok() {
        mockClient = MockRUDPClient(CommandResponse(ExitCode.OK, "Тип коллекции: Vector"))
        commandManager = CommandManager()
        commandManager.register(Info(mockClient, console, UserSession()))

        val exitCode = commandManager.getCommand("info").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is InfoRequest)
        val hasInfo = console.lines.any { it.contains("Тип коллекции") }
        assertEquals(true, hasInfo)
    }

    @Test
    fun help_should_return_ok_and_list_commands() {
        val exitCode = commandManager.getCommand("help").execute()

        assertEquals(ExitCode.OK, exitCode)
        val hasHelp = console.lines.any { it.contains("add") }
        assertEquals(true, hasHelp)
    }

    @Test
    fun exit_should_return_exit_code() {
        val exitCode = commandManager.getCommand("exit").execute()

        assertEquals(ExitCode.EXIT, exitCode)
    }

    @Test
    fun remove_last_should_send_remove_last_request() {
        val exitCode = commandManager.getCommand("remove_last").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is RemoveLastRequest)
    }

    @Test
    fun remove_by_id_should_send_remove_by_id_request() {
        console.reader = BufferedReader(InputStreamReader("1\n".byteInputStream()))

        val exitCode = commandManager.getCommand("remove_by_id").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is RemoveByIdRequest)
    }

    @Test
    fun undo_should_send_undo_request() {
        console.reader = BufferedReader(InputStreamReader("1\n".byteInputStream()))

        val exitCode = commandManager.getCommand("undo").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is UndoRequest)
    }

    @Test
    fun average_of_average_mark_should_send_request() {
        mockClient = MockRUDPClient(CommandResponse(ExitCode.OK, "42"))
        commandManager = CommandManager()
        commandManager.register(AverageOfAverageMark(mockClient, console, UserSession()))

        val exitCode = commandManager.getCommand("average_of_average_mark").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is AverageOfAverageMarkRequest)
        val hasNumber = console.lines.any { it.trim().toLongOrNull() != null }
        assertEquals(true, hasNumber)
    }

    @Test
    fun count_less_than_average_mark_should_send_request() {
        console.reader = BufferedReader(InputStreamReader("100\n".byteInputStream()))
        mockClient = MockRUDPClient(CommandResponse(ExitCode.OK, "3"))
        commandManager = CommandManager()
        commandManager.register(CountLessThanAverageMark(mockClient, console, UserSession()))

        val exitCode = commandManager.getCommand("count_less_than_average_mark").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is CountLessThanAverageMarkRequest)
    }

    @Test
    fun count_greater_than_average_mark_should_send_request() {
        console.reader = BufferedReader(InputStreamReader("5\n".byteInputStream()))
        mockClient = MockRUDPClient(CommandResponse(ExitCode.OK, "2"))
        commandManager = CommandManager()
        commandManager.register(CountGreaterThanAverageMark(mockClient, console, UserSession()))

        val exitCode = commandManager.getCommand("count_greater_than_average_mark").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(true, mockClient.lastRequest is CountGreaterThanAverageMarkRequest)
    }

    @Test
    fun command_should_return_error_when_server_returns_error() {
        mockClient = MockRUDPClient(CommandResponse(ExitCode.ERROR, "Что-то пошло не так"))
        commandManager = CommandManager()
        commandManager.register(Clear(mockClient, console, UserSession()))

        val exitCode = commandManager.getCommand("clear").execute()

        assertEquals(ExitCode.ERROR, exitCode)
    }
}