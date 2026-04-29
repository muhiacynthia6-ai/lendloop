package com.example.lendloop.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Query("SELECT * FROM reviews WHERE revieweeId = :userId ORDER BY createdAt DESC")
    fun getReviewsForUser(userId: Int): Flow<List<Review>>

    @Query("SELECT AVG(rating) FROM reviews WHERE revieweeId = :userId")
    suspend fun getAverageRating(userId: Int): Float?

    @Query("SELECT * FROM reviews WHERE recordId = :recordId LIMIT 1")
    suspend fun getReviewForRecord(recordId: Int): Review?

    @Query("SELECT COUNT(*) FROM reviews WHERE recordId = :recordId")
    suspend fun hasReview(recordId: Int): Int
}