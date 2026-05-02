package com.example.lendloop.data.repository

import com.example.lendloop.data.db.ElectronicsDao
import com.example.lendloop.data.db.ElectronicsDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElectronicsRepository @Inject constructor(
    private val dao: ElectronicsDao
) {
    suspend fun insertDetail(detail: ElectronicsDetail) = dao.insertDetail(detail)
    suspend fun getDetailForRecord(recordId: Int) = dao.getDetailForRecord(recordId)
}
