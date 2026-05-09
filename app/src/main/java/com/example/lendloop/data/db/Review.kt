package com.example.lendloop.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = BorrowRecord::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recordId: Int,
    val reviewerId: String,
    val revieweeId: String,
    val rating: Int,
    val comment: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
