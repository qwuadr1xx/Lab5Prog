import kotlinx.serialization.ExperimentalSerializationApi
import models.Coordinates
import models.StudyGroup
import net.requests.*
import net.responses.CommandResponse
import net.responses.IResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import utils.AppProtoBuf
import utils.ExitCode

@OptIn(ExperimentalSerializationApi::class)
class SerializationTest {

    private val testToken = "test-token"

    private fun encodeRequest(request: IRequest): ByteArray =
        AppProtoBuf.encodeToByteArray(IRequest.serializer(), request)

    private fun decodeRequest(bytes: ByteArray): IRequest =
        AppProtoBuf.decodeFromByteArray(IRequest.serializer(), bytes)

    private fun encodeResponse(response: IResponse): ByteArray =
        AppProtoBuf.encodeToByteArray(IResponse.serializer(), response)

    private fun decodeResponse(bytes: ByteArray): IResponse =
        AppProtoBuf.decodeFromByteArray(IResponse.serializer(), bytes)

    private fun minimalGroup(name: String = "Test", averageMark: Long = 10) = StudyGroup(
        name = name,
        coordinates = Coordinates(1L, 2.0),
        expelledStudents = 1,
        averageMark = averageMark,
        ownerId = null
    )

    // ─── StudyGroup ───────────────────────────────────────────────────────────

    @Test
    fun studyGroup_survives_protobuf_roundtrip() {
        val original = minimalGroup(name = "RoundtripGroup", averageMark = 42)
        val bytes = AppProtoBuf.encodeToByteArray(StudyGroup.serializer(), original)
        val decoded = AppProtoBuf.decodeFromByteArray(StudyGroup.serializer(), bytes)
        assertEquals(original.name, decoded.name)
        assertEquals(original.averageMark, decoded.averageMark)
        assertEquals(original.expelledStudents, decoded.expelledStudents)
        assertEquals(original.coordinates.x, decoded.coordinates.x)
        assertEquals(original.coordinates.y, decoded.coordinates.y)
        assertEquals(original.creationDate, decoded.creationDate)
    }

    // ─── Requests ─────────────────────────────────────────────────────────────

    @Test
    fun addRequest_roundtrip_preserves_study_group() {
        val group = minimalGroup(name = "AddGroup")
        val request = AddRequest(group, testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is AddRequest)
        assertEquals(group.name, (decoded as AddRequest).studyGroup.name)
    }

    @Test
    fun addIfMaxRequest_roundtrip() {
        val group = minimalGroup(name = "MaxGroup", averageMark = 99)
        val request = AddIfMaxRequest(group, testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is AddIfMaxRequest)
        assertEquals(99L, (decoded as AddIfMaxRequest).studyGroup.averageMark)
    }

    @Test
    fun showRequest_roundtrip() {
        val decoded = decodeRequest(encodeRequest(ShowRequest(testToken)))
        assertTrue(decoded is ShowRequest)
    }

    @Test
    fun infoRequest_roundtrip() {
        val decoded = decodeRequest(encodeRequest(InfoRequest(testToken)))
        assertTrue(decoded is InfoRequest)
    }

    @Test
    fun clearRequest_roundtrip() {
        val decoded = decodeRequest(encodeRequest(ClearRequest(testToken)))
        assertTrue(decoded is ClearRequest)
    }

    @Test
    fun removeLastRequest_roundtrip() {
        val decoded = decodeRequest(encodeRequest(RemoveLastRequest(testToken)))
        assertTrue(decoded is RemoveLastRequest)
    }

    @Test
    fun removeByIdRequest_roundtrip_preserves_id() {
        val request = RemoveByIdRequest(id = 42, testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is RemoveByIdRequest)
        assertEquals(42, (decoded as RemoveByIdRequest).id)
    }

    @Test
    fun insertAtRequest_roundtrip_preserves_index_and_group() {
        val group = minimalGroup(name = "InsertGroup")
        val request = InsertAtRequest(index = 3, studyGroup = group, testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is InsertAtRequest)
        val typedDecoded = decoded as InsertAtRequest
        assertEquals(3, typedDecoded.index)
        assertEquals("InsertGroup", typedDecoded.studyGroup.name)
    }

    @Test
    fun updateRequest_roundtrip_preserves_id_and_group() {
        val group = minimalGroup(name = "UpdatedGroup")
        val request = UpdateRequest(id = 7, studyGroup = group, testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is UpdateRequest)
        val typedDecoded = decoded as UpdateRequest
        assertEquals(7, typedDecoded.id)
        assertEquals("UpdatedGroup", typedDecoded.studyGroup.name)
    }

    @Test
    fun averageOfAverageMarkRequest_roundtrip() {
        val decoded = decodeRequest(encodeRequest(AverageOfAverageMarkRequest(testToken)))
        assertTrue(decoded is AverageOfAverageMarkRequest)
    }

    @Test
    fun countLessThanAverageMarkRequest_roundtrip_preserves_threshold() {
        val request = CountLessThanAverageMarkRequest(averageMark = 55, testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is CountLessThanAverageMarkRequest)
        assertEquals(55L, (decoded as CountLessThanAverageMarkRequest).averageMark)
    }

    @Test
    fun countGreaterThanAverageMarkRequest_roundtrip_preserves_threshold() {
        val request = CountGreaterThanAverageMarkRequest(averageMark = 20, testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is CountGreaterThanAverageMarkRequest)
        assertEquals(20L, (decoded as CountGreaterThanAverageMarkRequest).averageMark)
    }

    @Test
    fun undoRequest_roundtrip_preserves_n() {
        val request = UndoRequest(n = 3, testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is UndoRequest)
        assertEquals(3, (decoded as UndoRequest).n)
    }

    @Test
    fun executeScriptRequest_roundtrip_preserves_lines() {
        val scriptLines = listOf("add", "TestGroup", "1", "2", "10", "1", "5", "THIRD", "0")
        val request = ExecuteScriptRequest(scriptLines, testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is ExecuteScriptRequest)
        assertEquals(scriptLines, (decoded as ExecuteScriptRequest).lines)
    }

    @Test
    fun executeScriptRequest_empty_lines_roundtrip() {
        val request = ExecuteScriptRequest(emptyList(), testToken)
        val decoded = decodeRequest(encodeRequest(request))
        assertTrue(decoded is ExecuteScriptRequest)
        assertTrue((decoded as ExecuteScriptRequest).lines.isEmpty())
    }

    // ─── Responses ────────────────────────────────────────────────────────────

    @Test
    fun commandResponse_ok_roundtrip() {
        val response = CommandResponse(ExitCode.OK, "Элемент добавлен")
        val decoded = decodeResponse(encodeResponse(response))
        assertTrue(decoded is CommandResponse)
        val typedDecoded = decoded as CommandResponse
        assertEquals(ExitCode.OK, typedDecoded.exitCode)
        assertEquals("Элемент добавлен", typedDecoded.message)
    }

    @Test
    fun commandResponse_error_roundtrip() {
        val response = CommandResponse(ExitCode.ERROR, "Ошибка выполнения")
        val decoded = decodeResponse(encodeResponse(response))
        assertTrue(decoded is CommandResponse)
        assertEquals(ExitCode.ERROR, (decoded as CommandResponse).exitCode)
    }

    @Test
    fun commandResponse_empty_message_roundtrip() {
        val response = CommandResponse(ExitCode.OK)
        val decoded = decodeResponse(encodeResponse(response))
        assertTrue(decoded is CommandResponse)
        assertEquals("", (decoded as CommandResponse).message)
    }

    // ─── polymorphism ─────────────────────────────────────────────────────────

    @Test
    fun polymorphic_sealed_dispatch_works_for_all_request_types() {
        val requests: List<IRequest> = listOf(
            AddRequest(minimalGroup(), testToken),
            ShowRequest(testToken),
            InfoRequest(testToken),
            ClearRequest(testToken),
            RemoveLastRequest(testToken),
            RemoveByIdRequest(1, testToken),
            AverageOfAverageMarkRequest(testToken),
            CountLessThanAverageMarkRequest(10, testToken),
            CountGreaterThanAverageMarkRequest(5, testToken),
            UndoRequest(1, testToken),
            ExecuteScriptRequest(listOf("show"), testToken)
        )
        requests.forEach { original ->
            val decoded = decodeRequest(encodeRequest(original))
            assertEquals(original::class, decoded::class,
                "Polymorphic roundtrip failed for ${original::class.simpleName}")
        }
    }
}