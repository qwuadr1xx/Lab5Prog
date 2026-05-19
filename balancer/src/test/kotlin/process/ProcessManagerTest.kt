package process

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.qwuadrixx.balancer.process.ProcessManager
import ru.qwuadrixx.balancer.utils.IdGenerator
import java.io.File

class ProcessManagerTest {

    @AfterEach
    fun cleanLogs() {
        File(".").listFiles { f -> f.name.startsWith("log_") }?.forEach { it.delete() }
    }

    @Test
    fun `addServer rolls back id when java executable does not exist`() {
        val manager = ProcessManager(jarPath = "server.jar", javaPath = "/nonexistent/java_xyz")
        val idBefore = IdGenerator.get()

        assertThrows<Exception> { manager.addServer(29900) }

        assertEquals(idBefore, IdGenerator.get(), "ID должен быть откатан при ошибке запуска процесса")
        assertFalse(manager.processes.containsKey(idBefore), "Процесс не должен попасть в map при ошибке")
    }

    @Test
    fun `removeServer throws NoSuchElementException for unknown id`() {
        val manager = ProcessManager()
        assertThrows<NoSuchElementException> {
            manager.removeServer(Int.MAX_VALUE)
        }
    }

    @Test
    fun `processes map is unmodifiable`() {
        val manager = ProcessManager()
        assertThrows<UnsupportedOperationException> {
            @Suppress("UNCHECKED_CAST")
            (manager.processes as MutableMap<Int, Process>)[0] = ProcessBuilder("echo").start()
        }
    }

    @Test
    fun `addServer with real server jar registers process and removeServer destroys it`() {
        val jarPath = "../server.jar"
        val jar = File(jarPath)
        if (!jar.exists()) {
            println("Пропуск: server.jar не найден по пути ${jar.absolutePath}")
            return
        }

        val manager = ProcessManager(jarPath = jarPath)
        val id = manager.addServer(29901)

        assertTrue(manager.processes.containsKey(id), "Процесс должен быть в map")
        assertTrue(manager.processes[id]!!.isAlive, "Процесс должен быть живым")

        manager.removeServer(id)

        assertFalse(manager.processes.containsKey(id), "Процесс должен быть удалён из map")
    }

    @Test
    fun `IdGenerator rollback decrements counter correctly`() {
        val before = IdGenerator.get()
        val allocated = IdGenerator.getAndIncrement()
        assertEquals(before, allocated)
        assertEquals(before + 1, IdGenerator.get())

        IdGenerator.rollback()
        assertEquals(before, IdGenerator.get())
    }
}
