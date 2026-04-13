package net.assembler

import net.packet.RUDPPacket
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface IAssembler {
    val map: Map<Uuid, List<RUDPPacket>>

    fun addPacket(packet: RUDPPacket)

    fun isComplete(uuid: Uuid): Boolean

    fun assemble(uuid: Uuid): ByteArray
}