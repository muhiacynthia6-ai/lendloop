package com.example.lendloop.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class ItemCondition { NEW, GOOD, FAIR, POOR }

@Entity(
    tableName = "electronics_details",
    foreignKeys = [
        ForeignKey(
            entity = BorrowRecord::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ElectronicsDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recordId: Int,
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val condition: ItemCondition = ItemCondition.GOOD,
    val conditionNotes: String = "",
    val estimatedValue: Double? = null
)