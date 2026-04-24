package utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import net.packet.RUDPPacket
import net.requests.IRequest
import net.responses.IResponse
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class ExitCode {
    OK,
    ERROR,
    EXIT
}

@OptIn(ExperimentalSerializationApi::class, ExperimentalUuidApi::class)
fun RUDPPacketSplitter(request: IRequest): List<RUDPPacket> {
    val byteArray = ProtoBuf.encodeToByteArray<IRequest>(request)
    val chunkSize = 1024
    val totalChunks = (byteArray.size + chunkSize - 1) / chunkSize
    val uuid = Uuid.random()

    val list = ArrayList<RUDPPacket>(totalChunks)

    for (i in 0..<totalChunks) {
        val fromIndex = i * chunkSize
        val toIndex = min(fromIndex + chunkSize, byteArray.size)

        val tmpBuffer = byteArray.copyOfRange(fromIndex, toIndex)

        list.add(
            RUDPPacket(
                type = 0.toByte(),
                uuid = uuid,
                length = totalChunks,
                chunkIndex = i,
                serializedData = tmpBuffer
            )
        )
    }

    return list
}

@OptIn(ExperimentalSerializationApi::class, ExperimentalUuidApi::class)
fun RUDPPacketSplitter(response: IResponse): List<RUDPPacket> {
    val byteArray = ProtoBuf.encodeToByteArray<IResponse>(response)
    val chunkSize = 1024
    val totalChunks = (byteArray.size + chunkSize - 1) / chunkSize
    val uuid = Uuid.random()

    val list = ArrayList<RUDPPacket>(totalChunks)

    for (i in 0..<totalChunks) {
        val fromIndex = i * chunkSize
        val toIndex = min(fromIndex + chunkSize, byteArray.size)

        val tmpBuffer = byteArray.copyOfRange(fromIndex, toIndex)

        list.add(
            RUDPPacket(
                type = 0.toByte(),
                uuid = uuid,
                length = totalChunks,
                chunkIndex = i,
                serializedData = tmpBuffer
            )
        )
    }

    return list
}

@OptIn(ExperimentalSerializationApi::class)
fun requestDeserializer(byteArray: ByteArray): IRequest = ProtoBuf.decodeFromByteArray<IRequest>(byteArray)

@OptIn(ExperimentalSerializationApi::class)
fun responseDeserializer(byteArray: ByteArray): IResponse = ProtoBuf.decodeFromByteArray<IResponse>(byteArray)