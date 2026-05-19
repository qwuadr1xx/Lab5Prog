@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.di

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDateTime
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import ru.qwuadrixx.client.DbChangeListener
import ru.qwuadrixx.client.HealthCheckListener
import ru.qwuadrixx.client.RUDPServer
import ru.qwuadrixx.client.RUDPServerReceiver
import ru.qwuadrixx.client.RUDPServerSender
import java.net.DatagramSocket
import ru.qwuadrixx.managers.CollectionManager
import ru.qwuadrixx.managers.ICollectionManager
import ru.qwuadrixx.managers.IInMemoryCollection
import ru.qwuadrixx.managers.IUserManager
import ru.qwuadrixx.managers.RequestManager
import ru.qwuadrixx.managers.UserManager
import ru.qwuadrixx.repository.HistoryRepository
import ru.qwuadrixx.repository.IHistoryRepository
import ru.qwuadrixx.repository.IStudyGroupRepository
import ru.qwuadrixx.repository.ITokenRepository
import ru.qwuadrixx.repository.IUserRepository
import ru.qwuadrixx.repository.StudyGroupRepository
import ru.qwuadrixx.repository.TokenRepository
import ru.qwuadrixx.repository.UserRepository
import ru.qwuadrixx.service.StudyGroupService
import ru.qwuadrixx.service.TokenService
import ru.qwuadrixx.utils.StudyGroupFactory
import utils.TokenUtils

private val log = LoggerFactory.getLogger("ServerModule")

@Serializable
data class ServerConfig(
    val port: Int = 0,
    val maxRetries: Int = 3,
    val socketTimeoutMs: Int = 3000,
    val fixedPoolSize: Int = 4,
    val balancerHost: String = "localhost",
    val balancerPort: Int = 8080,
    val tokenSecret: String = "default-rudp-token-secret"
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
    val balancerHost = System.getenv("BALANCER_HOST") ?: base.balancerHost
    val balancerPort = System.getenv("BALANCER_PORT")?.toIntOrNull() ?: base.balancerPort
    val tokenSecret = System.getenv("TOKEN_SECRET") ?: base.tokenSecret
    return base.copy(port = port, balancerHost = balancerHost, balancerPort = balancerPort, tokenSecret = tokenSecret)
        .also { log.info("Конфигурация: port={}, balancer={}:{}", it.port, it.balancerHost, it.balancerPort) }
}

val serverModule = module {
    single { loadServerConfig() }
    single<Connection> {
        val url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/lab5"
        val user = System.getenv("DB_USER") ?: "qwuadrixx"
        val password = System.getenv("DB_PASSWORD") ?: "12345"
        val schema = System.getenv("DB_SCHEMA") ?: "s507981"
        val connection = DriverManager.getConnection(url, user, password)
        connection.createStatement().execute("SET SEARCH_PATH TO $schema")
        connection
    }
    single<DSLContext> { DSL.using(get<Connection>(), SQLDialect.POSTGRES) }
    single<IStudyGroupRepository> { StudyGroupRepository() }
    single<IUserRepository> { UserRepository() }
    single<IHistoryRepository> { HistoryRepository() }
    single<IUserManager> { UserManager() }
    single { TokenUtils(get<ServerConfig>().tokenSecret) }
    single<ITokenRepository> { TokenRepository() }
    single { TokenService(get(), get()) }
    single<IInMemoryCollection> {
        val repo = get<IStudyGroupRepository>()
        val cm = CollectionManager()
        val loaded = repo.findAll()
        synchronized(cm.collection) { cm.collection.addAll(loaded) }
        loaded.maxOfOrNull { it.id }?.let { StudyGroupFactory.syncIdGenerator(it) }
        cm.lastInitTime = LocalDateTime.now()
        log.info("Коллекция загружена из БД: {} элементов", cm.collection.size)
        cm
    }
    single<ICollectionManager> { StudyGroupService(get(), get()) }
    single<DatagramSocket> {
        val cfg = get<ServerConfig>()
        DatagramSocket(cfg.port).apply { soTimeout = cfg.socketTimeoutMs }
    }
    single { RequestManager(get(), get(), get(), get()) }
    single {
        val cfg = get<ServerConfig>()
        RUDPServerReceiver(get(), cfg.maxRetries, cfg.socketTimeoutMs)
    }
    single {
        val cfg = get<ServerConfig>()
        RUDPServerSender(get(), cfg.maxRetries, cfg.socketTimeoutMs)
    }
    single { HealthCheckListener(get(), get()) }
    single { DbChangeListener(get()) }
    single { RUDPServer(get(), get(), get(), get()) }
}