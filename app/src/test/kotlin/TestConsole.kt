import ru.qwuadrixx.app.utils.IConsole
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class TestConsole(
    override var fileMode: Boolean = false,
    override var reader: BufferedReader = BufferedReader(InputStreamReader(System.`in`))
) : IConsole {

    val lines = ArrayList<String>()

    override fun readLine(): String = reader.readLine() ?: ""

    override fun printLine(line: String) {
        lines.add(line)
    }

    override fun printObject(obj: Any) {
        lines.add(obj.toString())
    }

    override fun printError(exc: Exception) {

    }

    override fun printError(exc: Exception, message: String) {

    }

    override fun setFileMode(fileName: String) {
        val file = File("/Users/qwuadrixx/IdeaProjects/Lab5Prog/app/src/test/resources/$fileName")
        reader = BufferedReader(
            InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8)
        )
        fileMode = true
    }

    override fun setInteractiveMode() {
        reader = BufferedReader(InputStreamReader(System.`in`))
        fileMode = false
    }
}
