package com.example.lendloop.data.repository

import com.example.lendloop.data.db.TrustScore
import com.example.lendloop.data.db.TrustScoreDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrustScoreRepository @Inject constructor(
    private val dao: TrustScoreDao
) {
    suspend fun getOrCreateTrustScore(userId: Int): TrustScore {
        return dao.getTrustScore(userId) ?: run {
            val newScore = TrustScore(userId = userId)
            dao.insertOrUpdate(newScore)
            newScore
        }
    }

    fun observeTrustScore(userId: Int): Flow<TrustScore?> =
        dao.observeTrustScore(userId)

    suspend fun onItemBorrowed(userId: Int) {
        getOrCreateTrustScore(userId)
        dao.incrementBorrowed(userId)
    }

    suspend fun onItemReturned(userId: Int) {
        getOrCreateTrustScore(userId)
        dao.incrementReturned(userId)
    }

    suspend fun liftExpiredRestrictions(userId: Int) {
        dao.liftExpiredRestrictions(userId)
    }

    suspend fun isRestricted(userId: Int): Boolean {
        val score = dao.getTrustScore(userId) ?: return false
        // Auto-lift if restriction has expired
        if (score.isRestricted && score.restrictedUntil != null) {
            if (score.restrictedUntil < System.currentTimeMillis()) {
                dao.liftExpiredRestrictions(userId)
                return false
            }
        }
        return score.isRestricted
    }
}