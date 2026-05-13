@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.di

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.DriverManager
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import ru.qwuadrixx.client.CollectionSyncListener
import ru.qwuadrixx.client.CollectionSyncNotifier
import ru.qwuadrixx.client.HealthCheckListener
import ru.qwuadrixx.client.RUDPServer
import ru.qwuadrixx.managers.CollectionManager
import ru.qwuadrixx.managers.ICollectionManager
import ru.qwuadrixx.managers.IUserManager
import ru.qwuadrixx.managers.RequestManager
import ru.qwuadrixx.managers.UserManager
import ru.qwuadrixx.repository.HistoryRepository
import ru.qwuadrixx.repository.IHistoryRepository
import ru.qwuadrixx.repository.IStudyGroupRepository
import ru.qwuadrixx.repository.IUserRepository
import ru.qwuadrixx.repository.StudyGroupRepository
import ru.qwuadrixx.repository.UserRepository

private val log = LoggerFactory.getLogger("ServerModule")

@Serializable
data class ServerConfig(
    val port: Int = 8081,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000,
    val healthPortOffset: Int = 1000,
    val syncPortOffset: Int = 2000,
    val fixedPoolSize: Int = 4,
    val peers: List<String> = emptyList()
)

fun loadServerConfig(): ServerConfig {
    val stream = object {}.javaClass.getResourceAsStream("/server.yml")
        ?: return ServerConfig().also { log.warn("server.yml не найден, используются значения по умолчанию") }
    val base = try {
        Yaml.default.decodeFromString(ServerConfig.serializer(), stream.bufferedReader().readText())
    } catch (e: Exception) {
        log.error("Ошибка загрузки server.yml: {}", e.message)
        ServerConfig()
    }
    val port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: base.port
    val peers = System.getenv("SERVER_PEERS")
        ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        ?: base.peers
    return base.copy(port = port, peers = peers)
        .also { log.info("Конфигурация: port={}, peers={}", it.port, it.peers) }
}

val serverModule = module {
    single { loadServerConfig() }
    single<DSLContext> {
        val url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/studs"
        val user = System.getenv("DB_USER") ?: "s507981"
        val password = System.getenv("DB_PASSWORD") ?: "kePUxneY01AIT6p5"
        val schema = System.getenv("DB_SCHEMA") ?: "s507981"
        val connection = DriverManager.getConnection(url, user, password)
        connection.createStatement().execute("SET SEARCH_PATH TO $schema")
        DSL.using(connection, SQLDialect.POSTGRES)
    }
    single<IStudyGroupRepository> { StudyGroupRepository() }
    single<IUserRepository> { UserRepository() }
    single<IHistoryRepository> { HistoryRepository() }
    single<IUserManager> { UserManager() }
    single<ICollectionManager> { CollectionManager() }
    single { RequestManager(get(), get(), get(), get()) }
    single { HealthCheckListener() }
    single { CollectionSyncNotifier() }
    single { CollectionSyncListener() }
    single { RUDPServer(get()) }
}
