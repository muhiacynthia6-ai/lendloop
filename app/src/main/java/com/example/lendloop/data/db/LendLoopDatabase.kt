package com.example.lendloop.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Person::class,
        BorrowRecord::class,
        Category::class,
        User::class,
        Review::class,
        TrustScore::class,
        Payment::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LendLoopDatabase : RoomDatabase() {
    abstract fun borrowDao(): BorrowDao
    abstract fun userDao(): UserDao
    abstract fun reviewDao(): ReviewDao
    abstract fun trustScoreDao(): TrustScoreDao
    abstract fun paymentDao(): PaymentDao
}