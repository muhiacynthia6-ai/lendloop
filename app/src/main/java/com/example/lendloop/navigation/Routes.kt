package com.example.lendloop.navigation

import android.net.Uri

object Routes {

    // ─────────────────────────────────────────────────────────────────────────
    // Auth routes
    // ─────────────────────────────────────────────────────────────────────────

    const val WELCOME  = "welcome"
    const val LOGIN    = "login"
    const val REGISTER = "register"

    // ─────────────────────────────────────────────────────────────────────────
    // Main routes
    // ─────────────────────────────────────────────────────────────────────────

    const val HOME       = "home"
    const val ADD_RECORD = "add_record"
    const val HISTORY    = "history"
    const val PROFILE    = "profile"

    // ─────────────────────────────────────────────────────────────────────────
    // Detail routes (with arguments)
    // ─────────────────────────────────────────────────────────────────────────

    const val PERSON  = "person/{personId}"
    const val REVIEW  = "review/{recordId}/{revieweeId}"
    const val PAYMENT = "payment/{recordId}/{amount}/{personName}"

    // ─────────────────────────────────────────────────────────────────────────
    // Route builder functions
    // ─────────────────────────────────────────────────────────────────────────

    fun personRoute(personId: Int): String =
        "person/$personId"

    fun reviewRoute(recordId: Int, revieweeId: Int): String =
        "review/$recordId/$revieweeId"

    fun paymentRoute(recordId: Int, amount: Float, personName: String): String =
        "payment/$recordId/$amount/${Uri.encode(personName)}"
}
