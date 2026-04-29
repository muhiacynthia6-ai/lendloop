package com.example.lendloop.data.repository

import com.example.lendloop.data.db.User
import com.example.lendloop.data.db.UserDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun register(name: String, phone: String, pin: String): Result<User> {
        val exists = userDao.phoneExists(phone)
        if (exists > 0) {
            return Result.failure(Exception("Phone number already registered"))
        }
        val user = User(name = name, phone = phone, pin = pin)
        val id = userDao.insertUser(user)
        return Result.success(user.copy(id = id.toInt()))
    }

    suspend fun login(phone: String, pin: String): Result<User> {
        val user = userDao.login(phone, pin)
            ?: return Result.failure(Exception("Incorrect phone number or PIN"))
        return Result.success(user)
    }

    suspend fun phoneExists(phone: String): Boolean {
        return userDao.phoneExists(phone) > 0
    }
}