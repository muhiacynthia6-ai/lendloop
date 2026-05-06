package com.example.lendloop.navigation

import android.net.Uri

object Routes {
    const val WELCOME    = "welcome"
    const val LOGIN      = "login"
    const val REGISTER   = "register"
    const val HOME       = "home"
    const val ADD_RECORD = "add_record"
    const val HISTORY    = "history"
    const val PROFILE    = "profile"
    const val DASHBOARD  = "dashboard"
    const val PERSON      = "person/{personId}"
    const val EDIT_RECORD = "edit_record/{recordId}"
    const val REVIEW      = "review/{recordId}/{revieweeId}"
    const val PAYMENT     = "payment/{recordId}/{amount}/{personName}"

    fun personRoute(personId: Int)     = "person/$personId"
    fun editRecordRoute(recordId: Int) = "edit_record/$recordId"
    fun reviewRoute(recordId: Int, revieweeId: Int) = "review/$recordId/$revieweeId"
    fun paymentRoute(recordId: Int, amount: Double, personName: String) =
        "payment/$recordId/$amount/${Uri.encode(personName)}"
}