package com.example.lendloop.data.repository

import com.example.lendloop.data.db.Review
import com.example.lendloop.data.db.ReviewDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val dao: ReviewDao
) {
    suspend fun submitReview(review: Review): Long = dao.insertReview(review)
    fun getReviewsForUser(userId: String): Flow<List<Review>> = dao.getReviewsForUser(userId)
    suspend fun getAverageRating(userId: String): Float = dao.getAverageRating(userId) ?: 0f
    suspend fun hasReview(recordId: Int): Boolean = dao.hasReview(recordId) > 0
    suspend fun getReviewForRecord(recordId: Int): Review? = dao.getReviewForRecord(recordId)
}
