package com.example.lendloop.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectronicsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: ElectronicsDetail): Long

    @Update
    suspend fun updateDetail(detail: ElectronicsDetail)

    @Query("SELECT * FROM electronics_details WHERE recordId = :recordId LIMIT 1")
    suspend fun getDetailForRecord(recordId: Int): ElectronicsDetail?

    @Query("SELECT * FROM electronics_details WHERE recordId = :recordId LIMIT 1")
    fun observeDetailForRecord(recordId: Int): Flow<ElectronicsDetail?>

    @Delete
    suspend fun deleteDetail(detail: ElectronicsDetail)
}