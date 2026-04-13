package ru.qwuadrixx.client

import net.requests.IRequest
import java.net.DatagramSocket

class RUDPServer {
    private var datagramSocket: DatagramSocket = DatagramSocket(PORT)

    private fun receive(): IRequest {
        return null
    }



    companion object {
        private const val PORT = 8081
    }
}