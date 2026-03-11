import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.qwuadrixx.app.commands.*
import ru.qwuadrixx.app.managers.*
import ru.qwuadrixx.app.models.Coordinates
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.utils.ExitCode
import java.io.BufferedReader
import java.io.InputStreamReader


internal class CommandsTest {
    private lateinit var console: TestConsole
    private lateinit var commandManager: ICommandManager
    private lateinit var collectionManager: ICollectionManager

    @BeforeEach
    fun setUp() {
        console = TestConsole()
        val fileManager: IFileManager = FileManager(console, "/Users/qwuadrixx/IdeaProjects/Lab5Prog/app/src/test/resources/TestSaveFile.txt")
        collectionManager = CollectionManager(console = console, fileManager = fileManager)
        commandManager = CommandManager()

        commandManager.apply {
            register(Add(collectionManager, console))
            register(AddIfMax(collectionManager, console))
            register(Show(collectionManager, console))
            register(AverageOfAverageMark(collectionManager, console))
            register(Clear(collectionManager, console))
            register(CountLessThanAverageMark(collectionManager, console))
            register(CountGreaterThanAverageMark(collectionManager, console))
            register(Exit(console))
            register(Help(console, this))
            register(Info(collectionManager, console))
            register(InsertAt(collectionManager, console))
            register(RemoveById(collectionManager, console))
            register(RemoveLast(collectionManager, console))
            register(Update(collectionManager, console))
        }
    }

    @Test
    fun add_should_add_element_and_return_ok() {
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

        val initialSize = collectionManager.collection.size
        val exitCode = commandManager.getCommand("add").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(initialSize + 1, collectionManager.collection.size)
    }

    @Test
    fun show_should_return_ok() {
        val exitCode = commandManager.getCommand("show").execute()

        assertEquals(ExitCode.OK, exitCode)
        val hasOutput = console.lines.any { it.isNotBlank() }
        assertEquals(true, hasOutput)
    }

    @Test
    fun clear_should_clear_collection() {
        val exitCode = commandManager.getCommand("clear").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(0, collectionManager.collection.size)
    }

    @Test
    fun info_should_return_ok_and_output_info() {
        val exitCode = commandManager.getCommand("info").execute()

        assertEquals(ExitCode.OK, exitCode)
        val hasInfo = console.lines.any { it.contains("Тип коллекции") }
        assertEquals(true, hasInfo)
    }

    @Test
    fun help_should_return_ok_and_list_commands() {
        val exitCode = commandManager.getCommand("help").execute()

        assertEquals(ExitCode.OK, exitCode)
        val hasHelp = console.lines.any { it.contains("add") && it.contains("show") }
        assertEquals(true, hasHelp)
    }

    @Test
    fun exit_should_return_exit_code() {
        val exitCode = commandManager.getCommand("exit").execute()

        assertEquals(ExitCode.EXIT, exitCode)
    }

    @Test
    fun remove_last_should_decrease_size() {
        val initialSize = collectionManager.collection.size
        if (initialSize == 0) return

        val exitCode = commandManager.getCommand("remove_last").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(initialSize - 1, collectionManager.collection.size)
    }

    @Test
    fun insert_at_should_insert_element() {
        val initialSize = collectionManager.collection.size
        val newGroup = StudyGroup(
            name = "InsertedGroup",
            coordinates = Coordinates(1, 2.0),
            studentsCount = 10,
            expelledStudents = 1,
            averageMark = 5
        )

        collectionManager.insertAt(initialSize, newGroup)

        assertEquals(initialSize + 1, collectionManager.collection.size)
        assertEquals("InsertedGroup", collectionManager.collection[initialSize].name)
    }

    @Test
    fun collection_should_be_initialized_from_file() {
        val size = collectionManager.collection.size
        assertEquals(true, size > 0)
    }

    @Test
    fun remove_by_id_should_remove_element() {
        if (collectionManager.collection.isEmpty()) return
        val idToRemove = collectionManager.collection.first().id

        console.reader = BufferedReader(
            InputStreamReader("$idToRemove\n".byteInputStream())
        )

        val initialSize = collectionManager.collection.size
        val exitCode = commandManager.getCommand("remove_by_id").execute()

        assertEquals(ExitCode.OK, exitCode)
        assertEquals(initialSize - 1, collectionManager.collection.size)
    }

    @Test
    fun update_should_update_element() {
        if (collectionManager.collection.isEmpty()) return
        val idToUpdate = collectionManager.collection.first().id

        console.reader = BufferedReader(
            InputStreamReader(
                """
                $idToUpdate
                UpdatedGroup
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

        val exitCode = commandManager.getCommand("update").execute()

        assertEquals(ExitCode.OK, exitCode)
        val updated = collectionManager.collection.first { it.id == idToUpdate }
        assertEquals("UpdatedGroup", updated.name)
    }

    @Test
    fun average_of_average_mark_should_output_average() {
        val exitCode = commandManager.getCommand("average_of_average_mark").execute()

        assertEquals(ExitCode.OK, exitCode)
        val hasNumber = console.lines.any { it.trim().toLongOrNull() != null }
        assertEquals(true, hasNumber)
    }

    @Test
    fun count_less_than_average_mark_should_output_count() {
        console.reader = BufferedReader(
            InputStreamReader("100\n".byteInputStream())
        )

        val exitCode = commandManager.getCommand("count_less_than_average_mark").execute()

        assertEquals(ExitCode.OK, exitCode)
        val hasNumber = console.lines.any { it.trim().toLongOrNull() != null }
        assertEquals(true, hasNumber)
    }

    @Test
    fun count_greater_than_average_mark_should_output_count() {
        console.reader = BufferedReader(
            InputStreamReader("5\n".byteInputStream())
        )

        val exitCode = commandManager.getCommand("count_greater_than_average_mark").execute()

        assertEquals(ExitCode.OK, exitCode)
        val hasNumber = console.lines.any { it.trim().toLongOrNull() != null }
        assertEquals(true, hasNumber)
    }

    @Test
    fun add_if_max_should_not_reduce_or_break_collection() {
        if (collectionManager.collection.isEmpty()) return
        val maxAverage = collectionManager.collection.maxOf { it.averageMark }
        val nonMaxGroup = StudyGroup(
            name = "NonMax",
            coordinates = Coordinates(1, 2.0),
            studentsCount = 10,
            expelledStudents = 1,
            averageMark = maxAverage - 1
        )

        val initialSize = collectionManager.collection.size
        collectionManager.addIfMax(nonMaxGroup)

        assertEquals(initialSize, collectionManager.collection.size)
    }
}

