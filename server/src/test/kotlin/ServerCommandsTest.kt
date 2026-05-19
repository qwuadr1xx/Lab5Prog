import exception.ExistingLoginException
import models.Coordinates
import models.StudyGroup
import net.requests.*
import net.responses.CommandResponse
import net.responses.IResponse
import net.responses.LoginResponse
import net.responses.RegisterResponse
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import ru.qwuadrixx.generated.tables.references.*
import ru.qwuadrixx.di.ServerConfig
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
import utils.ExitCode
import utils.TokenUtils
import java.sql.DriverManager

private fun responseExitCode(response: IResponse): ExitCode = when (response) {
    is CommandResponse -> response.exitCode
    is LoginResponse -> response.exitCode
    is RegisterResponse -> response.exitCode
    else -> ExitCode.ERROR
}

private fun makeGroup(
    name: String = "TestGroup",
    averageMark: Long = 10,
    expelledStudents: Int = 1
) = StudyGroup(
    name = name,
    coordinates = Coordinates(1L, 2.0),
    expelledStudents = expelledStudents,
    averageMark = averageMark,
    ownerId = null
)

private fun addGroupLines(name: String, averageMark: Long = 10) = listOf(
    "add", name, "1", "2.0", "", "1", averageMark.toString(), "", "0"
)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerCommandsTest {

    private lateinit var rm: RequestManager
    private lateinit var cm: ICollectionManager
    private lateinit var dsl: DSLContext

    private val login = "test_${System.currentTimeMillis()}"
    private val password = "testPass123"
    private lateinit var token: String

    private val testModule = module {
        single<DSLContext> {
            val url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/itmo"
            val user = System.getenv("DB_USER") ?: "qwuadrixx"
            val pwd = System.getenv("DB_PASSWORD") ?: "12345"
            val schema = System.getenv("DB_SCHEMA") ?: "programming"
            val connection = DriverManager.getConnection(url, user, pwd)
            connection.createStatement().execute("SET SEARCH_PATH TO $schema")
            DSL.using(connection, SQLDialect.POSTGRES)
        }
        single { ServerConfig() }
        single<IStudyGroupRepository> { StudyGroupRepository() }
        single<IUserRepository> { UserRepository() }
        single<IHistoryRepository> { HistoryRepository() }
        single<IUserManager> { UserManager() }
        single<IInMemoryCollection> { CollectionManager() }
        single<ICollectionManager> { StudyGroupService(get(), get()) }
        single { TokenUtils("test-token-secret") }
        single<ITokenRepository> { TokenRepository() }
        single { TokenService(get(), get()) }
        single { RequestManager(get(), get(), get(), get()) }
    }

    private fun deleteUser(userLogin: String) {
        val uid = dsl.select(USERS.ID).from(USERS).where(USERS.LOGIN.eq(userLogin)).fetchOne(USERS.ID)
            ?: return
        val groups = dsl.selectFrom(STUDY_GROUP).where(STUDY_GROUP.CREATOR_ID.eq(uid)).fetch()
        val coordIds = groups.mapNotNull { it.coordinatesId }
        val adminIds = groups.mapNotNull { it.adminId }
        dsl.deleteFrom(SNAPSHOT_HISTORY).where(SNAPSHOT_HISTORY.AUTHOR_ID.eq(uid)).execute()
        dsl.deleteFrom(TOKENS).where(TOKENS.USER_ID.eq(uid)).execute()
        dsl.deleteFrom(STUDY_GROUP).where(STUDY_GROUP.CREATOR_ID.eq(uid)).execute()
        if (adminIds.isNotEmpty()) dsl.deleteFrom(PERSON).where(PERSON.ID.`in`(adminIds)).execute()
        if (coordIds.isNotEmpty()) dsl.deleteFrom(COORDINATES).where(COORDINATES.ID.`in`(coordIds)).execute()
        dsl.deleteFrom(USERS).where(USERS.ID.eq(uid)).execute()
    }

    private fun registerAndGetToken(login: String, pwd: String): String {
        val response = rm.dispatch(RegisterRequest(login, pwd)) as RegisterResponse
        return response.token
    }

    @BeforeAll
    fun startKoin() {
        startKoin { modules(testModule) }
        val koin = GlobalContext.get()
        dsl = koin.get()
        rm = koin.get()
        cm = koin.get()

        token = try {
            registerAndGetToken(login, password)
        } catch (_: ExistingLoginException) {
            (rm.dispatch(LoginRequest(login, password)) as LoginResponse).token
        }
    }

    @AfterAll
    fun stopKoinAndCleanup() {
        deleteUser(login)
        stopKoin()
    }

    @BeforeEach
    fun cleanDbAndMemory() {
        val uid = dsl.select(USERS.ID).from(USERS).where(USERS.LOGIN.eq(login)).fetchOne(USERS.ID)
        if (uid != null) {
            val groups = dsl.selectFrom(STUDY_GROUP).where(STUDY_GROUP.CREATOR_ID.eq(uid)).fetch()
            val coordIds = groups.mapNotNull { it.coordinatesId }
            val adminIds = groups.mapNotNull { it.adminId }
            dsl.deleteFrom(SNAPSHOT_HISTORY).execute()
            dsl.deleteFrom(STUDY_GROUP).where(STUDY_GROUP.CREATOR_ID.eq(uid)).execute()
            if (adminIds.isNotEmpty()) dsl.deleteFrom(PERSON).where(PERSON.ID.`in`(adminIds)).execute()
            if (coordIds.isNotEmpty()) dsl.deleteFrom(COORDINATES).where(COORDINATES.ID.`in`(coordIds)).execute()
        }
        cm.collection.clear()
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    fun register_new_user_returns_ok() {
        val newLogin = "reg_${System.currentTimeMillis()}"
        val response = rm.dispatch(RegisterRequest(newLogin, "somePass"))
        assertEquals(ExitCode.OK, responseExitCode(response))
        deleteUser(newLogin)
    }

    @Test
    fun register_duplicate_login_returns_error() {
        val response = rm.dispatch(RegisterRequest(login, password))
        assertEquals(ExitCode.ERROR, responseExitCode(response))
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    fun login_with_correct_credentials_returns_ok() {
        val response = rm.dispatch(LoginRequest(login, password))
        assertEquals(ExitCode.OK, responseExitCode(response))
    }

    @Test
    fun login_with_wrong_password_returns_error() {
        val response = rm.dispatch(LoginRequest(login, "wrongPassword"))
        assertEquals(ExitCode.ERROR, responseExitCode(response))
    }

    @Test
    fun login_with_unknown_user_returns_error() {
        val response = rm.dispatch(LoginRequest("nonexistent_user_xyz", "pass"))
        assertEquals(ExitCode.ERROR, responseExitCode(response))
    }

    // ── add ───────────────────────────────────────────────────────────────────

    @Test
    fun add_increases_collection_size() {
        val response = rm.dispatch(AddRequest(makeGroup(), token))
        assertEquals(ExitCode.OK, responseExitCode(response))
        assertEquals(1, cm.collection.size)
    }

    @Test
    fun add_element_is_present_in_collection() {
        rm.dispatch(AddRequest(makeGroup(name = "UniqueGroup"), token))
        assertTrue(cm.collection.any { it.name == "UniqueGroup" })
    }

    @Test
    fun add_assigns_non_zero_id_from_db() {
        rm.dispatch(AddRequest(makeGroup(), token))
        assertTrue(cm.collection.first().id > 0)
    }

    @Test
    fun add_sets_ownerId_from_db() {
        rm.dispatch(AddRequest(makeGroup(), token))
        assertNotNull(cm.collection.first().ownerId)
    }

    @Test
    fun add_multiple_elements_all_persisted_in_db() {
        rm.dispatch(AddRequest(makeGroup(name = "Alpha"), token))
        rm.dispatch(AddRequest(makeGroup(name = "Beta"), token))
        assertEquals(2, cm.collection.size)

        val uid = dsl.select(USERS.ID).from(USERS).where(USERS.LOGIN.eq(login)).fetchOne(USERS.ID)
        val dbCount = dsl.fetchCount(STUDY_GROUP, STUDY_GROUP.CREATOR_ID.eq(uid))
        assertEquals(2, dbCount)
    }

    // ── remove_by_id ──────────────────────────────────────────────────────────

    @Test
    fun remove_by_id_removes_correct_element() {
        rm.dispatch(AddRequest(makeGroup(name = "Keep"), token))
        rm.dispatch(AddRequest(makeGroup(name = "Remove"), token))
        val targetId = cm.collection.first { it.name == "Remove" }.id

        val response = rm.dispatch(RemoveByIdRequest(targetId, token))
        assertEquals(ExitCode.OK, responseExitCode(response))
        assertEquals(1, cm.collection.size)
        assertEquals("Keep", cm.collection.first().name)
    }

    @Test
    fun remove_by_id_returns_error_for_nonexistent_id() {
        val response = rm.dispatch(RemoveByIdRequest(Int.MAX_VALUE, token))
        assertEquals(ExitCode.ERROR, responseExitCode(response))
    }

    @Test
    fun remove_by_id_returns_error_for_wrong_owner() {
        val otherLogin = "other_${System.currentTimeMillis()}"
        val otherToken = registerAndGetToken(otherLogin, "otherPass")

        try {
            rm.dispatch(AddRequest(makeGroup(name = "OwnedGroup"), token))
            val id = cm.collection.first().id

            val response = rm.dispatch(RemoveByIdRequest(id, otherToken))
            assertEquals(ExitCode.ERROR, responseExitCode(response))
            assertEquals(1, cm.collection.size)
        } finally {
            deleteUser(otherLogin)
        }
    }

    // ── remove_last ───────────────────────────────────────────────────────────

    @Test
    fun remove_last_removes_last_element() {
        rm.dispatch(AddRequest(makeGroup(name = "First"), token))
        rm.dispatch(AddRequest(makeGroup(name = "Last"), token))
        val response = rm.dispatch(RemoveLastRequest(token))
        assertEquals(ExitCode.OK, responseExitCode(response))
        assertEquals(1, cm.collection.size)
    }

    @Test
    fun remove_last_on_empty_collection_returns_error() {
        val response = rm.dispatch(RemoveLastRequest(token))
        assertEquals(ExitCode.ERROR, responseExitCode(response))
    }

    // ── show / info ───────────────────────────────────────────────────────────

    @Test
    fun show_returns_ok_for_empty_collection() {
        val response = rm.dispatch(ShowRequest(token)) as CommandResponse
        assertEquals(ExitCode.OK, response.exitCode)
    }

    @Test
    fun info_returns_ok_with_collection_type() {
        val response = rm.dispatch(InfoRequest(token)) as CommandResponse
        assertEquals(ExitCode.OK, response.exitCode)
        assertTrue(response.message.contains("Тип коллекции"))
    }

    // ── clear ─────────────────────────────────────────────────────────────────

    @Test
    fun clear_removes_only_own_elements() {
        val otherLogin = "other2_${System.currentTimeMillis()}"
        val otherToken = registerAndGetToken(otherLogin, "otherPass2")

        try {
            rm.dispatch(AddRequest(makeGroup(name = "Mine"), token))
            rm.dispatch(AddRequest(makeGroup(name = "Theirs"), otherToken))

            rm.dispatch(ClearRequest(token))

            assertFalse(cm.collection.any { it.name == "Mine" })

            val theirsDbCount = dsl.fetchCount(STUDY_GROUP, STUDY_GROUP.NAME.eq("Theirs"))
            assertEquals(1, theirsDbCount)
        } finally {
            deleteUser(otherLogin)
            cm.collection.clear()
        }
    }

    // ── undo ──────────────────────────────────────────────────────────────────

    @Test
    fun undo_reverts_last_add() {
        rm.dispatch(AddRequest(makeGroup(), token))
        assertEquals(1, cm.collection.size)

        val response = rm.dispatch(UndoRequest(1, token))
        assertEquals(ExitCode.OK, responseExitCode(response))
        assertEquals(0, cm.collection.size)
    }

    @Test
    fun undo_on_empty_history_returns_error() {
        val response = rm.dispatch(UndoRequest(1, token))
        assertEquals(ExitCode.ERROR, responseExitCode(response))
    }

    @Test
    fun undo_reverts_multiple_steps() {
        rm.dispatch(AddRequest(makeGroup(name = "A"), token))
        rm.dispatch(AddRequest(makeGroup(name = "B"), token))

        val response = rm.dispatch(UndoRequest(2, token))
        assertEquals(ExitCode.OK, responseExitCode(response))
        assertEquals(0, cm.collection.size)
    }

    // ── execute_script ────────────────────────────────────────────────────────

    @Test
    fun execute_script_adds_elements_to_db() {
        val dbSizeBefore = dsl.fetchCount(STUDY_GROUP)
        val lines = addGroupLines("ScriptGroup1") + addGroupLines("ScriptGroup2")
        val response = rm.dispatch(ExecuteScriptRequest(lines, token))
        assertEquals(ExitCode.OK, responseExitCode(response))
        assertEquals(dbSizeBefore + 2, cm.collection.size)
        assertTrue(cm.collection.any { it.name == "ScriptGroup1" })
        assertTrue(cm.collection.any { it.name == "ScriptGroup2" })

        val dbCount = dsl.fetchCount(STUDY_GROUP, STUDY_GROUP.NAME.`in`("ScriptGroup1", "ScriptGroup2"))
        assertEquals(2, dbCount)
    }

    @Test
    fun execute_script_rollback_on_parse_error_leaves_collection_unchanged() {
        rm.dispatch(AddRequest(makeGroup(name = "Existing"), token))
        val sizeBefore = cm.collection.size

        val lines = addGroupLines("WillBeRolledBack") + listOf("remove_by_id", "not-a-number")
        val response = rm.dispatch(ExecuteScriptRequest(lines, token))

        assertEquals(ExitCode.ERROR, responseExitCode(response))
        assertEquals(sizeBefore, cm.collection.size)
        assertFalse(cm.collection.any { it.name == "WillBeRolledBack" })
    }

    @Test
    fun execute_script_empty_lines_returns_ok() {
        val response = rm.dispatch(ExecuteScriptRequest(emptyList(), token))
        assertEquals(ExitCode.OK, responseExitCode(response))
    }

    @Test
    fun execute_script_with_show_command_returns_ok() {
        rm.dispatch(AddRequest(makeGroup(name = "Visible"), token))
        val lines = listOf("show")
        val response = rm.dispatch(ExecuteScriptRequest(lines, token)) as CommandResponse
        assertEquals(ExitCode.OK, response.exitCode)
        assertTrue(response.message.contains("Visible"))
    }

    @Test
    fun execute_script_add_then_remove_existing_element() {
        rm.dispatch(AddRequest(makeGroup(name = "Existing"), token))
        val existingId = cm.collection.first().id

        val lines = addGroupLines("Added") + listOf("remove_by_id", existingId.toString())
        val response = rm.dispatch(ExecuteScriptRequest(lines, token))
        assertEquals(ExitCode.OK, responseExitCode(response))

        assertFalse(cm.collection.any { it.name == "Existing" })
        assertTrue(cm.collection.any { it.name == "Added" })
    }
}