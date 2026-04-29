package com.example.lendloop.data.remote

import android.util.Base64
import java.text.SimpleDateFormat
import java.util.*

object MpesaConfig {
    // ── Sandbox credentials (safe to use for testing) ──────────────────
    // Replace with real credentials when you get a Safaricom shortcode
    const val CONSUMER_KEY = "your_sandbox_consumer_key"
    const val CONSUMER_SECRET = "your_sandbox_consumer_secret"
    const val SHORTCODE = "174379"           // Safaricom sandbox shortcode
    const val PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    const val BASE_URL = "https://sandbox.safaricom.co.ke/"
    const val CALLBACK_URL = "https://webhook.site/your-unique-id" // use webhook.site for testing

    fun getBasicAuth(): String {
        val credentials = "$CONSUMER_KEY:$CONSUMER_SECRET"
        return "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"
    }

    fun getTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getPassword(timestamp: String): String {
        val str = "$SHORTCODE$PASSKEY$timestamp"
        return Base64.encodeToString(str.toByteArray(), Base64.NO_WRAP)
    }
}