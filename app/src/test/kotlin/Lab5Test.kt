import org.junit.jupiter.api.BeforeEach
import ru.qwuadrixx.app.managers.*
import ru.qwuadrixx.app.utils.Console
import ru.qwuadrixx.app.utils.IConsole

class Lab5Test {
    private lateinit var fileName: String
    private lateinit var console: IConsole
    private lateinit var commandManager: ICommandManager
    private lateinit var fileManager: IFileManager
    private lateinit var collectionManager: ICollectionManager


    @BeforeEach
    fun setUp() {
        fileName = "TestSaveFile.txt"
        console = Console()
        commandManager = CommandManager()
        fileManager = FileManager(console = console, fileName = fileName)
        collectionManager = CollectionManager(console = console, fileManager = fileManager)
    }
}