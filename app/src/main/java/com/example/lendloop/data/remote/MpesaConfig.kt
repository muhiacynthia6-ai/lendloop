package com.example.lendloop.data.remote

import android.util.Base64
import java.text.SimpleDateFormat
import java.util.*

object MpesaConfig {
    // ── Paste your Daraja sandbox credentials here ──────────────────────
    const val CONSUMER_KEY    = "jWuwAUIAZAkKq9SyeSGi9g63WqAOFiXXpfVLzp3xWRgdcocU"
    const val CONSUMER_SECRET = "PwR19BC4odWwf12cSGxKtmEqw2uqX9RsN9v32YBa94xlAOevJhMdritzAzXORdk6"

    // ── Sandbox shortcode + passkey (these are Safaricom's official test values) ──
    const val SHORTCODE   = "174379"
    const val PASSKEY     = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    const val BASE_URL    = "https://sandbox.safaricom.co.ke/"

    // ── Use webhook.site for testing callbacks ──────────────────────────
    // Go to webhook.site, copy your unique URL and paste it here
    const val CALLBACK_URL = "https://webhook.site/your-unique-url"

    fun getBasicAuth(): String {
        val credentials = "$CONSUMER_KEY:$CONSUMER_SECRET"
        return "Basic ${Base64.encodeToString(
            credentials.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )}"
    }

    fun getTimestamp(): String {
        return SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            .format(Date())
    }

    fun getPassword(timestamp: String): String {
        val raw = "$SHORTCODE$PASSKEY$timestamp"
        return Base64.encodeToString(
            raw.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
    }

    object MpesaConfig {
        const val BASE_URL = "https://sandbox.safaricom.co.ke/"
        const val SHORTCODE = "174379"
        const val PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919" // sandbox key
        const val CALLBACK_URL = "https://yourdomain.com/callback"

        fun getTimestamp(): String {
            val sdf = java.text.SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault())
            return sdf.format(java.util.Date())
        }

        fun getBasicAuth(): String {
            val credentials = "jWuwAUIAZAkKq9SyeSGi9g63WqAOFiXXpfVLzp3xWRgdcocU:PwR19BC4odWwf12cSGxKtmEqw2uqX9RsN9v32YBa94xlAOevJhMdritzAzXORdk6"
            return "Basic ${android.util.Base64.encodeToString(
                credentials.toByteArray(), android.util.Base64.NO_WRAP
            )}"
        }
    }
}