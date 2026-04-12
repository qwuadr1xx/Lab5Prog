package net.packet

import java.nio.ByteBuffer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.getUuid
import kotlin.uuid.putUuid

@OptIn(ExperimentalUuidApi::class)
class RUDPPacket(val type: Byte, val uuid: Uuid, val length: Int, val chunkIndex: Int, val serializedData: ByteArray) {
    fun toByteBuffer(): ByteBuffer = ByteBuffer.allocate(HEADING + serializedData.size).apply {
        put(type)
        putUuid(uuid)
        putInt(length)
        putInt(chunkIndex)
        put(serializedData)
    }

    companion object {
        const val HEADING = 25

        fun fromByteBuffer(byteBuffer: ByteBuffer): RUDPPacket {
            val type = byteBuffer.get()
            val uuid = byteBuffer.getUuid()
            val length = byteBuffer.getInt()
            val chunkIndex = byteBuffer.getInt()

            val data = ByteArray(byteBuffer.remaining())
            byteBuffer.get(data)

            return RUDPPacket(type, uuid, length, chunkIndex, data)
        }

        fun ACK(): ByteBuffer = ByteBuffer.allocate(1).put(1.toByte())

        fun PING(): ByteBuffer = ByteBuffer.allocate(1).put(2.toByte())

        fun FIN(): ByteBuffer = ByteBuffer.allocate(1).put(3.toByte())

        fun isASK(byteBuffer: ByteBuffer): Boolean {
            val type = byteBuffer.get()
            return type == 1.toByte()
        }

        fun isPING(byteBuffer: ByteBuffer): Boolean {
            val type = byteBuffer.get()
            return type == 2.toByte()
        }

        fun isFIN(byteBuffer: ByteBuffer): Boolean {
            val type = byteBuffer.get()
            return type == 3.toByte()
        }
    }
}