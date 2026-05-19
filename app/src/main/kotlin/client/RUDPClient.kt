package ru.qwuadrixx.app.client

import net.assembler.IAssembler
import net.requests.IRequest
import net.responses.IResponse
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.qwuadrixx.app.di.ClientConfig
import utils.RUDPPacketSplitter
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPClient(assembler: IAssembler) : KoinComponent, IRUDPClient {

    private val config: ClientConfig by inject()
    private val serverAddress = InetSocketAddress(config.serverHost, config.serverPort)
    private val datagramChannel: DatagramChannel = DatagramChannel.open().apply {
        configureBlocking(false)
    }
    private val sender = RUDPSender(datagramChannel, serverAddress, config.maxRetries, config.socketTimeoutMs.toLong())
    private val receiver = RUDPReceiver(datagramChannel, assembler, sender, config.maxRetries, config.socketTimeoutMs.toLong())

    override fun sendAndReceive(request: IRequest): IResponse {
        val packets = RUDPPacketSplitter(request)
        sender.ping()
        sender.sendPackets(packets)
        return receiver.receiveResponse()
    }

    override fun close() {
        datagramChannel.close()
    }
}