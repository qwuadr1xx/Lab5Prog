package ru.qwuadrixx.managers

import org.slf4j.LoggerFactory
import ru.qwuadrixx.utils.StudyGroupFactory
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds

class FileWatcher(
    private val fm: IFileManager,
    private val cm: ICollectionManager
) : Runnable {

    private val logger = LoggerFactory.getLogger(FileWatcher::class.java)

    override fun run() {
        val filePath = Paths.get(fm.fileName).toAbsolutePath().normalize()
        val dir = filePath.parent ?: Paths.get(".").toAbsolutePath()

        FileSystems.getDefault().newWatchService().use { watchService ->
            dir.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE)

            logger.info("FileWatcher запущен для '{}'", fm.fileName)

            while (!Thread.currentThread().isInterrupted) {
                val key = try {
                    watchService.take()
                } catch (_: InterruptedException) {
                    break
                }

                for (event in key.pollEvents()) {
                    val changedPath = dir.resolve(event.context() as Path).normalize()
                    if (changedPath != filePath) continue

                    Thread.sleep(50)

                    val result = fm.readCollection() ?: continue
                    val (fileVersion, data) = result

                    if (fileVersion.isNotEmpty() && fileVersion == cm.currentVersion) {
                        logger.debug("FileWatcher: версия {} совпадает — перезагрузка пропущена", fileVersion)
                        continue
                    }

                    synchronized(cm.collection) {
                        cm.collection.clear()
                        cm.collection.addAll(data)
                        data.forEach { StudyGroupFactory.syncIdGenerator(it.id) }
                    }
                    cm.currentVersion = fileVersion
                    logger.info("Коллекция синхронизирована из файла (версия '{}')", fileVersion)
                }

                key.reset()
            }
        }
    }
}