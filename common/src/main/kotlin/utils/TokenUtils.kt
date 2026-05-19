package utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import models.Token
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@OptIn(ExperimentalSerializationApi::class)
class TokenUtils(private val secret: String) {

    fun encode(token: Token): String {
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(AppProtoBuf.encodeToByteArray(Token.serializer(), token))
        val signature = hmac(payload)
        return "$payload.$signature"
    }

    fun decode(encoded: String): Token? {
        val dot = encoded.lastIndexOf('.')
        if (dot < 0) return null
        val payload = encoded.substring(0, dot)
        val signature = encoded.substring(dot + 1)
        if (hmac(payload) != signature) return null
        return try {
            AppProtoBuf.decodeFromByteArray(Token.serializer(), Base64.getUrlDecoder().decode(payload))
        } catch (_: Exception) {
            null
        }
    }

    fun isValid(token: Token): Boolean = System.currentTimeMillis() < token.expiresAt

    private fun hmac(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(mac.doFinal(data.toByteArray(Charsets.UTF_8)))
    }
}