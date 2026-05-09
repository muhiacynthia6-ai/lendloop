package com.example.lendloop.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trust_scores")
data class TrustScore(
    @PrimaryKey
    val userId: String,
    val totalBorrowed: Int = 0,
    val totalReturned: Int = 0,
    val returnRate: Float = 100f,
    val isRestricted: Boolean = false,
    val restrictedUntil: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
