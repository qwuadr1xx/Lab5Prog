package utils

import kotlinx.serialization.Serializable

@Serializable
enum class ServerFilterArg {
    ALL,
    ENABLED,
    DISABLED,
    AVAILABLE,
    UNAVAILABLE
}