package com.example.lendloop.data.remote

import android.util.Base64
import java.text.SimpleDateFormat
import java.util.*
object MpesaConfig {
    const val CONSUMER_KEY    = "18Q0fqJoFWd4LuhsyfmwI4cpbhtRAauGB02GOVHqWAV3wnYI"
    const val CONSUMER_SECRET = "jP0zh2btSeivA3pqkQfM2CpAJsfrW0murUPADOUJfxLC7CPYOJ9NbDuxodPB6pFI"
    const val SHORTCODE       = "174379"
    const val PASSKEY         = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    const val BASE_URL        = "https://sandbox.safaricom.co.ke/"
    const val CALLBACK_URL    = "https://webhook.site/99a3b0e6-e97b-4838-83fb-972d253a7569"

    fun getBasicAuth(): String {
        val credentials = "$CONSUMER_KEY:$CONSUMER_SECRET"
        return "Basic ${Base64.encodeToString(
            credentials.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )}"
    }

    fun getTimestamp(): String =
        SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())

    fun getPassword(timestamp: String): String {
        val raw = "$SHORTCODE$PASSKEY$timestamp"
        return Base64.encodeToString(raw.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }
}