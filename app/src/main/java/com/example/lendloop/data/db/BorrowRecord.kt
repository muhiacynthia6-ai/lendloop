package com.example.lendloop.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "borrow_records",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BorrowRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val personId: Int,
    val personName: String,
    val itemName: String,
    val category: String = "Other",
    val direction: Direction,
    val amount: Double? = null,
    val lentAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val photoUri: String? = null,
    val note: String? = null,
    val status: Status = Status.ACTIVE,
    val returnedAt: Long? = null,
    val lastRemindedAt: Long? = null
)

enum class Direction { LENT, BORROWED }
enum class Status { ACTIVE, RETURNED }