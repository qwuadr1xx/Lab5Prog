package models

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val id: String,
    val createdAt: Long,
    val expiresAt: Long,
    val userId: Long,
    val login: String,
    val isAdmin: Boolean
)