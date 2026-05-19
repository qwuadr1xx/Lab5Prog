package ru.qwuadrixx.client

import org.postgresql.PGConnection
import org.slf4j.LoggerFactory
import ru.qwuadrixx.managers.ICollectionManager
import java.sql.DriverManager

class DbChangeListener(private val cm: ICollectionManager) : Runnable {

    private val logger = LoggerFactory.getLogger(DbChangeListener::class.java)

    override fun run() {
        val url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/lab5"
        val user = System.getenv("DB_USER") ?: "qwuadrixx"
        val password = System.getenv("DB_PASSWORD") ?: "12345"
        val schema = System.getenv("DB_SCHEMA") ?: "s507981"

        try {
            DriverManager.getConnection(url, user, password).use { conn ->
                conn.createStatement().execute("SET SEARCH_PATH TO $schema")
                conn.createStatement().execute("LISTEN study_group_changed")
                val pgConn = conn.unwrap(PGConnection::class.java)
                logger.info("DbChangeListener запущен, слушаю study_group_changed")

                while (!Thread.currentThread().isInterrupted) {
                    val notifications = pgConn.getNotifications(5000)
                    if (!notifications.isNullOrEmpty()) {
                        logger.debug("Получено {} уведомлений из БД, перезагружаю коллекцию", notifications.size)
                        cm.reloadFromDb()
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("DbChangeListener завершился с ошибкой: {}", e.message, e)
        }
    }
}