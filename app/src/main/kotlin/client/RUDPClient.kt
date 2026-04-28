package ru.qwuadrixx.app.client

import net.assembler.IAssembler
import net.requests.IRequest
import net.responses.IResponse
import utils.RUDPPacketSplitter
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPClient(
    assembler: IAssembler,
    serverHost: String,
    serverPort: Int,
    maxRetries: Int,
    socketTimeoutMs: Int
) : IRUDPClient {

    private val serverAddress = InetSocketAddress(serverHost, serverPort)
    private val datagramChannel: DatagramChannel = DatagramChannel.open().apply {
        configureBlocking(false)
    }

    private val sender = RUDPSender(datagramChannel, serverAddress, maxRetries, socketTimeoutMs.toLong())
    private val receiver = RUDPReceiver(datagramChannel, assembler, sender, maxRetries, socketTimeoutMs.toLong())

    override fun sendAndReceive(request: IRequest): IResponse {
        val packets = RUDPPacketSplitter(request)
        sender.ping()
        sender.sendPackets(packets)
        return receiver.receiveResponse()
    }
}
