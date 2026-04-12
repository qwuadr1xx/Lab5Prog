package net.responses

import utils.ExitCode

interface IResponse {
    val exitCode: ExitCode
    val errorMessage: String?
    val value: Any?
}