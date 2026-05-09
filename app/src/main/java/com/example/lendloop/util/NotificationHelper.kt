package com.example.lendloop.util

import com.example.lendloop.util.toFormattedDate

object NotificationHelper {

    fun buildBorrowerThankYouMessage(
        borrowerName: String,
        itemName: String,
        amount: Double?
    ): String {
        return if (amount != null && amount > 0) {
            "Hi $borrowerName 😊 Thank you for returning/reminding me about Ksh ${amount.toInt()} for \"$itemName\" 🙌"
        } else {
            "Hi $borrowerName 😊 Thank you for returning \"$itemName\" 🙌"
        }
    }

    fun buildReturnReminderMessage(
        borrowerName: String,
        itemName: String,
        lentDate: Long,
        amount: Double?
    ): String {
        val dateStr = lentDate.toFormattedDate()
        return if (amount != null && amount > 0) {
            "Hi $borrowerName! Just a friendly reminder about the Ksh ${amount.toInt()} for \"$itemName\" lent on $dateStr. Please let me know when you can return it. Thanks! 📋"
        } else {
            "Hi $borrowerName! Just a friendly reminder about the \"$itemName\" lent on $dateStr. Please let me know when you can return it. Thanks! 📋"
        }
    }
}
