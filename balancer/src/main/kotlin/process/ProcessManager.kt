package ru.qwuadrixx.balancer.process

import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.utils.IdGenerator
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class ProcessManager(
    private val jarPath: String = "server.jar",
    private val javaPath: String = "java"
) : IProcessManager {

    private val _processes = ConcurrentHashMap<Int, Process>()
    private val log = LoggerFactory.getLogger(ProcessManager::class.java)

    override val processes: Map<Int, Process>
        get() = Collections.unmodifiableMap(_processes)

    override fun addServer(port: Int): Int {
        val id = IdGenerator.get()
        try {
            val logsDir = File("logs").also { it.mkdirs() }
            val logFile = File(logsDir, "log_$port")
            val process = ProcessBuilder(javaPath, "-jar", jarPath)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
                .also { it.environment()["SERVER_PORT"] = port.toString() }
                .start()

            _processes[id] = process
            log.info("Сервер запущен: port={}, pid={}, id={}, log={}", port, process.pid(), id, logFile.path)
            return id
        } catch (e: Exception) {
            IdGenerator.rollback()
            log.error("Ошибка запуска сервера port={}, откат id: {}", port, e.message)
            throw e
        }
    }

    override fun removeServer(id: Int) {
        val process = _processes.remove(id)
            ?: throw NoSuchElementException("Процесс с id=$id не найден в ProcessManager")
        try {
            process.destroy()
            log.info("Процесс id={} остановлен", id)
        } catch (e: Exception) {
            log.error("Ошибка при остановке процесса id={}: {}", id, e.message)
            throw e
        }
    }
}