package com.example.lendloop.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Query("SELECT * FROM payments WHERE recordId = :recordId ORDER BY createdAt DESC")
    fun getPaymentsForRecord(recordId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Int): Payment?

    @Query("""
        UPDATE payments 
        SET status = 'CONFIRMED', paidAt = :paidAt 
        WHERE id = :id
    """)
    suspend fun confirmPayment(id: Int, paidAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM payments WHERE recordId = :recordId AND status = 'CONFIRMED' LIMIT 1")
    suspend fun getConfirmedPayment(recordId: Int): Payment?
}