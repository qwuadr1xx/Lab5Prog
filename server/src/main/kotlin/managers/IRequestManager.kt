package ru.qwuadrixx.managers

import net.requests.IRequest
import net.responses.IResponse

interface IRequestManager {
    fun dispatch(request: IRequest): IResponse
}