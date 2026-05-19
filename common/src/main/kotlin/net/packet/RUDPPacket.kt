package net.packet

import java.nio.ByteBuffer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.getUuid
import kotlin.uuid.putUuid

@ExperimentalUuidApi
class RUDPPacket(val type: Byte, val uuid: Uuid, val length: Int, val chunkIndex: Int, val serializedData: ByteArray) {
    fun toByteBuffer(): ByteBuffer = ByteBuffer.allocate(HEADING + serializedData.size).apply {
        put(type)
        putUuid(uuid)
        putInt(length)
        putInt(chunkIndex)
        put(serializedData)
        flip()
    }

    fun toByteArray(): ByteArray = ByteBuffer.allocate(HEADING + serializedData.size).apply {
        put(type)
        putUuid(uuid)
        putInt(length)
        putInt(chunkIndex)
        put(serializedData)
    }.array()

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

        fun fromByteArray(byteArray: ByteArray): RUDPPacket {
            val buffer = ByteBuffer.wrap(byteArray)
            val type = buffer.get()
            val uuid = buffer.getUuid()
            val length = buffer.getInt()
            val chunkIndex = buffer.getInt()

            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            return RUDPPacket(type, uuid, length, chunkIndex, data)
        }

        fun byteBufferACK(): ByteBuffer = ByteBuffer.allocate(1).put(1.toByte())

        fun byteBufferPING(): ByteBuffer = ByteBuffer.allocate(1).put(2.toByte())

        fun byteBufferFIN(): ByteBuffer = ByteBuffer.allocate(1).put(3.toByte())

        fun byteArrayACK(): ByteArray = byteArrayOf(1)

        fun byteArrayPING(): ByteArray = byteArrayOf(2)

        fun byteArrayFIN(): ByteArray = byteArrayOf(3)

        fun byteArrayBALANCERPING(): ByteArray = byteArrayOf(4)

        fun isByteBufferACK(byteBuffer: ByteBuffer): Boolean {
            val type = byteBuffer.get()
            return type == 1.toByte()
        }

        fun isByteBufferPING(byteBuffer: ByteBuffer): Boolean {
            val type = byteBuffer.get()
            return type == 2.toByte()
        }

        fun isByteBufferFIN(byteBuffer: ByteBuffer): Boolean {
            val type = byteBuffer.get()
            return type == 3.toByte()
        }

        fun isByteArrayACK(byteArray: ByteArray): Boolean = byteArray.isNotEmpty() && byteArray[0] == 1.toByte()

        fun isByteArrayPING(byteArray: ByteArray): Boolean = byteArray.isNotEmpty() && byteArray[0] == 2.toByte()

        fun isByteArrayFIN(byteArray: ByteArray): Boolean = byteArray.isNotEmpty() && byteArray[0] == 3.toByte()

        fun isByteArrayBALANCERPING(byteArray: ByteArray): Boolean = byteArray.isNotEmpty() && byteArray[0] == 4.toByte()
    }
}