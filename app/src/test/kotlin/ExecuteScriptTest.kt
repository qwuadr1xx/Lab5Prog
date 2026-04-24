import net.requests.ExecuteScriptRequest
import net.responses.CommandResponse
import net.responses.IResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.commands.ExecuteScript
import utils.ExitCode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import kotlin.io.path.writeText

class ExecuteScriptTest {

    private class MockClient(private val response: IResponse) : IRUDPClient {
        var lastRequest: net.requests.IRequest? = null
        override fun sendAndReceive(request: net.requests.IRequest): IResponse {
            lastRequest = request
            return response
        }
    }

    private fun makeClient(response: IResponse = CommandResponse(ExitCode.OK, "")) = MockClient(response)

    @Test
    fun executeScript_sends_raw_lines_to_server() {
        val client = makeClient()
        val console = TestConsole()
        val command = ExecuteScript(client, console)

        val dir = Files.createTempDirectory("lab6")
        val script = dir.resolve("script.txt")
        script.writeText(
            """
            add
            Group1
            1
            2
            10
            1
            5
            THIRD
            0
            """.trimIndent()
        )

        console.reader = BufferedReader(InputStreamReader("${script.toAbsolutePath()}\n".byteInputStream()))
        command.execute()

        val sentRequest = client.lastRequest
        assertNotNull(sentRequest)
        assertTrue(sentRequest is ExecuteScriptRequest)
        val sentLines = (sentRequest as ExecuteScriptRequest).lines
        assertTrue(sentLines.isNotEmpty())
        assertEquals("add", sentLines.first())
        assertFalse(sentLines.any { it == "execute_script" }, "Raw lines must not contain execute_script directives")
    }

    @Test
    fun executeScript_expands_nested_script_inline() {
        val client = makeClient()
        val console = TestConsole()
        val command = ExecuteScript(client, console)

        val dir = Files.createTempDirectory("lab6")
        val inner = dir.resolve("inner.txt")
        inner.writeText("clear")

        val outer = dir.resolve("outer.txt")
        outer.writeText(
            """
            show
            execute_script
            ${inner.toAbsolutePath()}
            info
            """.trimIndent()
        )

        console.reader = BufferedReader(InputStreamReader("${outer.toAbsolutePath()}\n".byteInputStream()))
        command.execute()

        val sentLines = (client.lastRequest as ExecuteScriptRequest).lines
        assertFalse(sentLines.any { it == "execute_script" })
        assertTrue(sentLines.contains("show"))
        assertTrue(sentLines.contains("clear"))
        assertTrue(sentLines.contains("info"))
    }

    @Test
    fun executeScript_detects_self_referencing_recursion() {
        val client = makeClient()
        val console = TestConsole()
        val command = ExecuteScript(client, console)

        val dir = Files.createTempDirectory("lab6")
        val selfRef = dir.resolve("self.txt")
        selfRef.writeText(
            """
            execute_script
            ${selfRef.toAbsolutePath()}
            """.trimIndent()
        )

        console.reader = BufferedReader(InputStreamReader("${selfRef.toAbsolutePath()}\n".byteInputStream()))
        val exitCode = command.execute()

        assertEquals(ExitCode.ERROR, exitCode)
        assertNull(client.lastRequest)
    }

    @Test
    fun executeScript_detects_indirect_recursion() {
        val client = makeClient()
        val console = TestConsole()
        val command = ExecuteScript(client, console)

        val dir = Files.createTempDirectory("lab6")
        val fileA = dir.resolve("a.txt")
        val fileB = dir.resolve("b.txt")
        fileA.writeText("execute_script\n${fileB.toAbsolutePath()}")
        fileB.writeText("execute_script\n${fileA.toAbsolutePath()}")

        console.reader = BufferedReader(InputStreamReader("${fileA.toAbsolutePath()}\n".byteInputStream()))
        val exitCode = command.execute()

        assertEquals(ExitCode.ERROR, exitCode)
        assertNull(client.lastRequest)
    }

    @Test
    fun executeScript_returns_error_for_nonexistent_file() {
        val client = makeClient()
        val console = TestConsole()
        val command = ExecuteScript(client, console)

        console.reader = BufferedReader(InputStreamReader("/no/such/file.txt\n".byteInputStream()))
        val exitCode = command.execute()

        assertEquals(ExitCode.ERROR, exitCode)
        assertNull(client.lastRequest)
    }

    @Test
    fun executeScript_returns_ok_for_empty_file() {
        val client = makeClient()
        val console = TestConsole()
        val command = ExecuteScript(client, console)

        val dir = Files.createTempDirectory("lab6")
        val empty = dir.resolve("empty.txt")
        empty.writeText("")

        console.reader = BufferedReader(InputStreamReader("${empty.toAbsolutePath()}\n".byteInputStream()))
        val exitCode = command.execute()

        assertEquals(ExitCode.OK, exitCode)
        assertNull(client.lastRequest)
    }

    @Test
    fun executeScript_propagates_server_exit_code() {
        val client = makeClient(CommandResponse(ExitCode.ERROR, "Ошибка сервера"))
        val console = TestConsole()
        val command = ExecuteScript(client, console)

        val dir = Files.createTempDirectory("lab6")
        val script = dir.resolve("s.txt")
        script.writeText("clear")

        console.reader = BufferedReader(InputStreamReader("${script.toAbsolutePath()}\n".byteInputStream()))
        val exitCode = command.execute()

        assertEquals(ExitCode.ERROR, exitCode)
    }
}
