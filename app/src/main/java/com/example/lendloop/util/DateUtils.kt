package com.example.lendloop.util

import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.daysAgo(): String {
    val diff = System.currentTimeMillis() - this
    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
    return when {
        days == 0 -> "Today"
        days == 1 -> "Yesterday"
        else -> "$days days ago"
    }
}

fun Long.isOverdue(): Boolean {
    return this < System.currentTimeMillis()
}

fun Long.daysUntilDue(): Int {
    val diff = this - System.currentTimeMillis()
    return (diff / (1000 * 60 * 60 * 24)).toInt()
}