package com.example.lendloop.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class PaymentMethod { CASH }
enum class PaymentStatus { PENDING, CONFIRMED, FAILED }

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = BorrowRecord::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id:        Int           = 0,
    val recordId:  Int,
    val method:    PaymentMethod = PaymentMethod.CASH,
    val amount:    Double,
    val status:    PaymentStatus = PaymentStatus.PENDING,
    val paidAt:    Long?         = null,
    val createdAt: Long          = System.currentTimeMillis()
)