package net.assembler

import net.packet.RUDPPacket
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class Assembler(override val map: MutableMap<Uuid, MutableList<RUDPPacket>> = HashMap()) : IAssembler {
    override fun addPacket(packet: RUDPPacket) {
        val packets = map.getOrPut(packet.uuid) { mutableListOf() }
        if (packets.any { it.chunkIndex == packet.chunkIndex }) return
        packets.add(packet)
    }

    override fun isComplete(uuid: Uuid): Boolean {
        val packets = map[uuid] ?: return false
        if (packets.isEmpty()) return false

        return packets.first().length == packets.size
    }

    override fun assemble(uuid: Uuid): ByteArray {
        val packets = map[uuid]!!
        val totalSize = packets.sumOf { it.serializedData.size }

        val result = ByteArray(totalSize)
        var currentOffset = 0

        for (packet in packets) {
            packet.serializedData.copyInto(
                destination = result,
                destinationOffset = currentOffset
            )
            currentOffset += packet.serializedData.size
        }

        return result
    }

}