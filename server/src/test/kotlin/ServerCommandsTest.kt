import models.Coordinates
import models.StudyGroup
import net.requests.*
import net.responses.CommandResponse
import net.responses.IResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.qwuadrixx.managers.CollectionManager
import ru.qwuadrixx.managers.IFileManager
import ru.qwuadrixx.managers.RequestManager
import utils.ExitCode
import java.util.Vector

// ─── Test infrastructure ──────────────────────────────────────────────────────

/** No-op file manager — avoids disk I/O in tests */
class NoOpFileManager : IFileManager {
    override val fileName = "test.csv"
    override fun writeCollection(collection: Collection<StudyGroup>) {}
    override fun readCollection(): List<StudyGroup>? = null
}

fun exitCode(response: IResponse): ExitCode = (response as CommandResponse).exitCode
fun message(response: IResponse): String = (response as CommandResponse).message

fun makeStudyGroup(
    name: String = "TestGroup",
    averageMark: Long = 10,
    expelledStudents: Int = 1
) = StudyGroup(
    name = name,
    coordinates = Coordinates(1L, 2.0),
    expelledStudents = expelledStudents,
    averageMark = averageMark
)

// ─── Tests ────────────────────────────────────────────────────────────────────

class ServerCommandsTest {

    private lateinit var collectionManager: CollectionManager
    private lateinit var requestManager: RequestManager

    @BeforeEach
    fun setUp() {
        collectionManager = CollectionManager()
        requestManager = RequestManager(collectionManager, NoOpFileManager())
    }

    // ── add ───────────────────────────────────────────────────────────────────

    @Test
    fun add_increases_collection_size() {
        val initialSize = collectionManager.collection.size
        val response = requestManager.dispatch(AddRequest(makeStudyGroup()))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(initialSize + 1, collectionManager.collection.size)
    }

    @Test
    fun add_element_is_present_in_collection() {
        val group = makeStudyGroup(name = "Unique")
        requestManager.dispatch(AddRequest(group))
        assertTrue(collectionManager.collection.any { it.name == "Unique" })
    }

    // ── add_if_max ────────────────────────────────────────────────────────────

    @Test
    fun add_if_max_adds_element_when_it_exceeds_all_existing() {
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 5)))
        val initialSize = collectionManager.collection.size
        val response = requestManager.dispatch(AddIfMaxRequest(makeStudyGroup(averageMark = 100)))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(initialSize + 1, collectionManager.collection.size)
    }

    @Test
    fun add_if_max_does_not_add_when_element_is_not_max() {
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 50)))
        val initialSize = collectionManager.collection.size
        val response = requestManager.dispatch(AddIfMaxRequest(makeStudyGroup(averageMark = 10)))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(initialSize, collectionManager.collection.size)
    }

    @Test
    fun add_if_max_adds_to_empty_collection() {
        val response = requestManager.dispatch(AddIfMaxRequest(makeStudyGroup()))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(1, collectionManager.collection.size)
    }

    // ── show ──────────────────────────────────────────────────────────────────

    @Test
    fun show_returns_ok_for_empty_collection() {
        val response = requestManager.dispatch(ShowRequest())
        assertEquals(ExitCode.OK, exitCode(response))
        assertTrue(message(response).contains("пуст"))
    }

    @Test
    fun show_returns_all_elements_when_collection_is_not_empty() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "Alpha")))
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "Beta")))
        val response = requestManager.dispatch(ShowRequest())
        assertEquals(ExitCode.OK, exitCode(response))
        val msg = message(response)
        assertTrue(msg.contains("Alpha"))
        assertTrue(msg.contains("Beta"))
    }

    // ── info ──────────────────────────────────────────────────────────────────

    @Test
    fun info_returns_collection_type_and_size() {
        requestManager.dispatch(AddRequest(makeStudyGroup()))
        val response = requestManager.dispatch(InfoRequest())
        assertEquals(ExitCode.OK, exitCode(response))
        val msg = message(response)
        assertTrue(msg.contains("Тип коллекции"))
        assertTrue(msg.contains("1"))
    }

    // ── clear ─────────────────────────────────────────────────────────────────

    @Test
    fun clear_empties_a_non_empty_collection() {
        requestManager.dispatch(AddRequest(makeStudyGroup()))
        requestManager.dispatch(AddRequest(makeStudyGroup()))
        val response = requestManager.dispatch(ClearRequest())
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(0, collectionManager.collection.size)
    }

    @Test
    fun clear_on_empty_collection_returns_ok() {
        val response = requestManager.dispatch(ClearRequest())
        assertEquals(ExitCode.OK, exitCode(response))
    }

    // ── remove_last ───────────────────────────────────────────────────────────

    @Test
    fun remove_last_removes_the_last_element() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "First")))
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "Last")))
        val response = requestManager.dispatch(RemoveLastRequest())
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(1, collectionManager.collection.size)
        assertEquals("First", collectionManager.collection.first().name)
    }

    @Test
    fun remove_last_on_empty_collection_returns_error() {
        val response = requestManager.dispatch(RemoveLastRequest())
        assertEquals(ExitCode.ERROR, exitCode(response))
    }

    // ── remove_by_id ──────────────────────────────────────────────────────────

    @Test
    fun remove_by_id_removes_the_correct_element() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "Keep")))
        val group = makeStudyGroup(name = "Remove")
        requestManager.dispatch(AddRequest(group))
        val targetId = collectionManager.collection.first { it.name == "Remove" }.id

        val response = requestManager.dispatch(RemoveByIdRequest(targetId))
        assertEquals(ExitCode.OK, exitCode(response))
        assertTrue(collectionManager.collection.none { it.id == targetId })
    }

    @Test
    fun remove_by_id_returns_error_for_nonexistent_id() {
        val response = requestManager.dispatch(RemoveByIdRequest(Int.MAX_VALUE))
        assertEquals(ExitCode.ERROR, exitCode(response))
    }

    // ── insert_at ─────────────────────────────────────────────────────────────

    @Test
    fun insert_at_places_element_at_given_index() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "A")))
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "B")))
        val group = makeStudyGroup(name = "Middle")
        val response = requestManager.dispatch(InsertAtRequest(1, group))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals("Middle", collectionManager.collection[1].name)
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    fun update_replaces_element_with_matching_id() {
        val original = makeStudyGroup(name = "Original")
        requestManager.dispatch(AddRequest(original))
        val id = collectionManager.collection.first().id
        // The replacement must carry the same id so the command can locate it later
        val replacement = StudyGroup(id = id, name = "Updated", coordinates = Coordinates(1L, 2.0), expelledStudents = 1, averageMark = 10)

        val response = requestManager.dispatch(UpdateRequest(id, replacement))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals("Updated", collectionManager.collection.first().name)
    }

    @Test
    fun update_returns_error_for_nonexistent_id() {
        val response = requestManager.dispatch(UpdateRequest(Int.MAX_VALUE, makeStudyGroup()))
        assertEquals(ExitCode.ERROR, exitCode(response))
    }

    // ── average_of_average_mark ───────────────────────────────────────────────

    @Test
    fun average_of_average_mark_returns_zero_for_empty_collection() {
        val response = requestManager.dispatch(AverageOfAverageMarkRequest())
        assertEquals(ExitCode.OK, exitCode(response))
        assertTrue(message(response).contains("0"))
    }

    @Test
    fun average_of_average_mark_calculates_mean_correctly() {
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 10)))
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 20)))
        val response = requestManager.dispatch(AverageOfAverageMarkRequest())
        assertEquals(ExitCode.OK, exitCode(response))
        assertTrue(message(response).contains("15"))
    }

    // ── count_less_than_average_mark ──────────────────────────────────────────

    @Test
    fun count_less_than_average_mark_counts_correctly() {
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 5)))
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 15)))
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 25)))
        val response = requestManager.dispatch(CountLessThanAverageMarkRequest(20))
        assertEquals(ExitCode.OK, exitCode(response))
        assertTrue(message(response).contains("2"))
    }

    @Test
    fun count_less_than_average_mark_returns_zero_when_none_qualify() {
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 50)))
        val response = requestManager.dispatch(CountLessThanAverageMarkRequest(10))
        assertEquals(ExitCode.OK, exitCode(response))
        assertTrue(message(response).contains("0"))
    }

    // ── count_greater_than_average_mark ───────────────────────────────────────

    @Test
    fun count_greater_than_average_mark_counts_correctly() {
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 5)))
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 15)))
        requestManager.dispatch(AddRequest(makeStudyGroup(averageMark = 25)))
        val response = requestManager.dispatch(CountGreaterThanAverageMarkRequest(10))
        assertEquals(ExitCode.OK, exitCode(response))
        assertTrue(message(response).contains("2"))
    }

    // ── undo ──────────────────────────────────────────────────────────────────

    @Test
    fun undo_reverts_last_add() {
        requestManager.dispatch(AddRequest(makeStudyGroup()))
        val sizeAfterAdd = collectionManager.collection.size
        val response = requestManager.dispatch(UndoRequest(1))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(sizeAfterAdd - 1, collectionManager.collection.size)
    }

    @Test
    fun undo_reverts_last_clear() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "A")))
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "B")))
        requestManager.dispatch(ClearRequest())
        assertEquals(0, collectionManager.collection.size)

        val response = requestManager.dispatch(UndoRequest(1))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(2, collectionManager.collection.size)
    }

    @Test
    fun undo_reverts_multiple_commands_in_order() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "One")))
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "Two")))
        val response = requestManager.dispatch(UndoRequest(2))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(0, collectionManager.collection.size)
    }

    @Test
    fun undo_on_empty_history_returns_error() {
        val response = requestManager.dispatch(UndoRequest(1))
        assertEquals(ExitCode.ERROR, exitCode(response))
    }

    @Test
    fun undo_reverts_remove_by_id() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "Kept")))
        val id = collectionManager.collection.first().id
        requestManager.dispatch(RemoveByIdRequest(id))
        assertEquals(0, collectionManager.collection.size)

        requestManager.dispatch(UndoRequest(1))
        assertEquals(1, collectionManager.collection.size)
        assertEquals("Kept", collectionManager.collection.first().name)
    }

    @Test
    fun undo_reverts_update() {
        val original = makeStudyGroup(name = "Original")
        requestManager.dispatch(AddRequest(original))
        val id = collectionManager.collection.first().id
        requestManager.dispatch(UpdateRequest(id, makeStudyGroup(name = "Changed")))

        requestManager.dispatch(UndoRequest(1))
        assertEquals("Original", collectionManager.collection.first { it.id == id }.name)
    }

    // ── execute_script ────────────────────────────────────────────────────────

    // ── execute_script with raw script lines ──────────────────────────────────

    /** Helper — raw lines for adding a StudyGroup via script syntax */
    private fun addGroupLines(name: String, averageMark: Long = 10) = listOf(
        "add", name, "1", "2", "", "1", averageMark.toString(), "THIRD", "0"
    )

    @Test
    fun execute_script_parses_and_executes_add_lines() {
        val scriptLines = addGroupLines("ScriptGroup1") + addGroupLines("ScriptGroup2")
        val response = requestManager.dispatch(ExecuteScriptRequest(scriptLines))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(2, collectionManager.collection.size)
        assertTrue(collectionManager.collection.any { it.name == "ScriptGroup1" })
        assertTrue(collectionManager.collection.any { it.name == "ScriptGroup2" })
    }

    @Test
    fun execute_script_rolls_back_all_changes_when_any_command_fails() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "PreExisting")))
        val sizeBeforeScript = collectionManager.collection.size

        // add one group, then try to remove a non-existent id → parse succeeds but execute fails
        val scriptLines = addGroupLines("ShouldBeRolledBack") +
                listOf("remove_by_id", Int.MAX_VALUE.toString())

        val response = requestManager.dispatch(ExecuteScriptRequest(scriptLines))

        assertEquals(ExitCode.ERROR, exitCode(response))
        assertEquals(sizeBeforeScript, collectionManager.collection.size)
        assertTrue(collectionManager.collection.none { it.name == "ShouldBeRolledBack" })
    }

    @Test
    fun execute_script_rollback_restores_history() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "BeforeScript")))

        val scriptLines = addGroupLines("ScriptAdd") +
                listOf("remove_by_id", Int.MAX_VALUE.toString())
        requestManager.dispatch(ExecuteScriptRequest(scriptLines))

        // History only contains the pre-script add; undo removes it
        val response = requestManager.dispatch(UndoRequest(1))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(0, collectionManager.collection.size)
    }

    @Test
    fun execute_script_returns_ok_for_empty_lines() {
        val response = requestManager.dispatch(ExecuteScriptRequest(emptyList()))
        assertEquals(ExitCode.OK, exitCode(response))
    }

    @Test
    fun execute_script_skips_unknown_command_names() {
        val scriptLines = listOf("unknown_command") + addGroupLines("AfterUnknown")
        val response = requestManager.dispatch(ExecuteScriptRequest(scriptLines))
        assertEquals(ExitCode.OK, exitCode(response))
        assertEquals(1, collectionManager.collection.size)
        assertEquals("AfterUnknown", collectionManager.collection.first().name)
    }

    @Test
    fun execute_script_accumulates_output_from_all_commands() {
        val scriptLines = addGroupLines("G1") + listOf("show")
        val response = requestManager.dispatch(ExecuteScriptRequest(scriptLines))
        assertEquals(ExitCode.OK, exitCode(response))
        assertTrue(message(response).contains("G1"))
    }

    @Test
    fun execute_script_parse_error_rolls_back() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "PreExisting")))
        val sizeBeforeScript = collectionManager.collection.size

        // add group first, then remove_by_id with non-numeric id → parse error
        val scriptLines = addGroupLines("WillBeRolledBack") + listOf("remove_by_id", "not-a-number")
        val response = requestManager.dispatch(ExecuteScriptRequest(scriptLines))

        assertEquals(ExitCode.ERROR, exitCode(response))
        assertEquals(sizeBeforeScript, collectionManager.collection.size)
    }

    // ── unknown command ───────────────────────────────────────────────────────

    @Test
    fun dispatch_returns_ok_for_all_known_read_only_commands() {
        requestManager.dispatch(AddRequest(makeStudyGroup()))
        // All read-only commands should succeed
        assertEquals(ExitCode.OK, exitCode(requestManager.dispatch(InfoRequest())))
        assertEquals(ExitCode.OK, exitCode(requestManager.dispatch(ShowRequest())))
        assertEquals(ExitCode.OK, exitCode(requestManager.dispatch(AverageOfAverageMarkRequest())))
        assertEquals(ExitCode.OK, exitCode(requestManager.dispatch(CountLessThanAverageMarkRequest(100))))
        assertEquals(ExitCode.OK, exitCode(requestManager.dispatch(CountGreaterThanAverageMarkRequest(1))))
    }

    // ── snapshot / restore ────────────────────────────────────────────────────

    @Test
    fun collection_manager_snapshot_preserves_state() {
        collectionManager.add(makeStudyGroup(name = "A"))
        collectionManager.add(makeStudyGroup(name = "B"))
        val snapshot = collectionManager.takeSnapshot()

        collectionManager.clear()
        assertEquals(0, collectionManager.collection.size)

        collectionManager.restoreSnapshot(snapshot)
        assertEquals(2, collectionManager.collection.size)
        assertTrue(collectionManager.collection.any { it.name == "A" })
        assertTrue(collectionManager.collection.any { it.name == "B" })
    }

    // ── sequential command interactions ───────────────────────────────────────

    @Test
    fun add_then_update_then_undo_restores_original() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "Original")))
        val id = collectionManager.collection.first().id
        val replacement = StudyGroup(id = id, name = "Updated", coordinates = Coordinates(1L, 2.0), expelledStudents = 1, averageMark = 10)

        requestManager.dispatch(UpdateRequest(id, replacement))
        assertEquals("Updated", collectionManager.collection.first().name)

        requestManager.dispatch(UndoRequest(1))
        assertEquals("Original", collectionManager.collection.first().name)
    }

    @Test
    fun clear_then_undo_then_remove_last_leaves_empty_collection() {
        requestManager.dispatch(AddRequest(makeStudyGroup(name = "Only")))
        requestManager.dispatch(ClearRequest())
        requestManager.dispatch(UndoRequest(1))     // restores "Only"
        assertEquals(1, collectionManager.collection.size)

        requestManager.dispatch(RemoveLastRequest())
        assertEquals(0, collectionManager.collection.size)
    }
}
