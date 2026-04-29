package com.example.lendloop.data.db

import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE phone = :phone AND pin = :pin LIMIT 1")
    suspend fun login(phone: String, pin: String): User?

    @Query("SELECT COUNT(*) FROM users WHERE phone = :phone")
    suspend fun phoneExists(phone: String): Int
}