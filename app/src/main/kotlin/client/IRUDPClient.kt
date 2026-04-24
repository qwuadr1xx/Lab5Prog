package ru.qwuadrixx.app.client

import net.requests.IRequest
import net.responses.IResponse

interface IRUDPClient {
    fun sendAndReceive(request: IRequest): IResponse
}