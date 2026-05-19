package models

import kotlinx.serialization.Serializable

@Serializable
data class ServerNodeInfo(
    val id: Int,
    val host: String,
    val port: Int,
    val isEnabled: Boolean,
    val isAvailable: Boolean,
    val isMain: Boolean,
    val createdAt: Long,
    val notAvailableSince: Long?
)