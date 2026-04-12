package ru.qwuadrixx.client

import java.net.DatagramSocket

class UDPServer {
    private var datagramSocket: DatagramSocket = DatagramSocket(PORT)

    companion object {
        private const val PORT = 8081
    }
}