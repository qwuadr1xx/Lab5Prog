import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.qwuadrixx.app.commands.*
import ru.qwuadrixx.app.managers.*
import ru.qwuadrixx.app.utils.ExitCode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.nio.file.Files
import kotlin.io.path.writeText

class ExecuteScriptTest {

    private fun createBaseContext(): Triple<ICommandManager, ICollectionManager, TestConsole> {
        val console = TestConsole()
        val fileManager: IFileManager = FileManager(console, "app/src/test/resources/TestSaveFile.txt")
        val collectionManager: ICollectionManager =
            CollectionManager(console = console, collection = Vector(fileManager.readCollection() ?: emptyList()))
        val commandManager: ICommandManager = CommandManager()

        commandManager.apply {
            register(Add(collectionManager, console))
            register(AddIfMax(collectionManager, console))
            register(Show(collectionManager, console))
            register(AverageOfAverageMark(collectionManager, console))
            register(Clear(collectionManager, console))
            register(CountLessThanAverageMark(collectionManager, console))
            register(CountGreaterThanAverageMark(collectionManager, console))
            register(ExecuteScript(this, collectionManager, fileManager, console))
            register(Exit(console))
            register(Help(console, this))
            register(Info(collectionManager, console))
            register(InsertAt(collectionManager, console))
            register(RemoveById(collectionManager, console))
            register(RemoveLast(collectionManager, console))
            register(Update(collectionManager, console))
        }

        return Triple(commandManager, collectionManager, console)
    }

    @Test
    fun executeScript_runs_script1_and_nested_script2() {
        val (commandManager, collectionManager, console) = createBaseContext()
        val initialSize = collectionManager.collection.size

        val dir = Files.createTempDirectory("lab5-scripts")
        val script2 = dir.resolve("script2.txt")
        val script1 = dir.resolve("script1.txt")

        script2.writeText(
            """
            add
            Group2
            3
            4
            20
            2
            10
            SECOND
            0
            
            """.trimIndent()
        )

        script1.writeText(
            """
            execute_script
            ${script2.toAbsolutePath()}
            
            """.trimIndent()
        )

        console.reader = BufferedReader(
            InputStreamReader("${script1.toAbsolutePath()}\n".byteInputStream())
        )

        val exitCode = commandManager.getCommand("execute_script").execute()

        assertEquals(ExitCode.ERROR, exitCode)
        assertEquals(initialSize, collectionManager.collection.size)
    }

    @Test
    fun executeScript_recursion_in_script3_returns_error() {
        val (commandManager, collectionManager, console) = createBaseContext()
        val initialSize = collectionManager.collection.size

        val dir = Files.createTempDirectory("lab5-scripts")
        val script = dir.resolve("script3.txt")
        script.writeText(
            """
            execute_script
            ${script.toAbsolutePath()}
            
            """.trimIndent()
        )

        console.reader = BufferedReader(
            InputStreamReader("${script.toAbsolutePath()}\n".byteInputStream())
        )

        val exitCode = commandManager.getCommand("execute_script").execute()

        assertEquals(ExitCode.ERROR, exitCode)
        assertEquals(initialSize, collectionManager.collection.size)
    }
}

