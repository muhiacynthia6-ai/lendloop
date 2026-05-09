package com.example.lendloop.data.repository

import com.example.lendloop.data.db.Payment
import com.example.lendloop.data.db.PaymentDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val dao: PaymentDao
) {
    suspend fun insertPayment(payment: Payment): Long = dao.insertPayment(payment)
    suspend fun confirmPayment(id: Int) = dao.confirmPayment(id)
    fun getPaymentsForRecord(recordId: Int): Flow<List<Payment>> =
        dao.getPaymentsForRecord(recordId)
    fun getAllConfirmedPayments(): Flow<List<Payment>> =
        dao.getAllConfirmedPayments()
    suspend fun getConfirmedPayment(recordId: Int): Payment? =
        dao.getConfirmedPayment(recordId)
    suspend fun getPaymentById(id: Int): Payment? = dao.getPaymentById(id)
}