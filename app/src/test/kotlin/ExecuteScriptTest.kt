import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.qwuadrixx.app.commands.*
import ru.qwuadrixx.app.managers.*
import ru.qwuadrixx.app.utils.ExitCode
import java.io.BufferedReader
import java.io.InputStreamReader

class ExecuteScriptTest {

    private fun createBaseContext(): Triple<ICommandManager, ICollectionManager, TestConsole> {
        val console = TestConsole()
        val fileManager: IFileManager = FileManager(console, "/Users/qwuadrixx/IdeaProjects/Lab5Prog/app/src/test/resources/TestSaveFile.txt")
        val collectionManager: ICollectionManager = CollectionManager(console = console, fileManager = fileManager)
        val commandManager: ICommandManager = CommandManager()

        commandManager.apply {
            register(Add(collectionManager, console))
            register(AddIfMax(collectionManager, console))
            register(Show(collectionManager, console))
            register(AverageOfAverageMark(collectionManager, console))
            register(Clear(collectionManager, console))
            register(CountLessThanAverageMark(collectionManager, console))
            register(CountGreaterThanAverageMark(collectionManager, console))
            register(ExecuteScript(this, collectionManager, console))
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

        console.reader = BufferedReader(
            InputStreamReader("script1.txt\n".byteInputStream())
        )

        val exitCode = commandManager.getCommand("execute_script").execute()

        assertEquals(ExitCode.ERROR, exitCode)
        assertEquals(initialSize, collectionManager.collection.size)
    }

    @Test
    fun executeScript_recursion_in_script3_returns_error() {
        val (commandManager, collectionManager, console) = createBaseContext()
        val initialSize = collectionManager.collection.size

        console.reader = BufferedReader(
            InputStreamReader("script3.txt\n".byteInputStream())
        )

        val exitCode = commandManager.getCommand("execute_script").execute()

        assertEquals(ExitCode.ERROR, exitCode)
        assertEquals(initialSize, collectionManager.collection.size)
    }
}

