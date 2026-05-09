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
    suspend fun getOrCreateTrustScore(userId: String): TrustScore {
        return dao.getTrustScore(userId) ?: run {
            val newScore = TrustScore(userId = userId)
            dao.insertOrUpdate(newScore)
            newScore
        }
    }

    fun observeTrustScore(userId: String): Flow<TrustScore?> =
        dao.observeTrustScore(userId)

    suspend fun onItemBorrowed(userId: String) {
        getOrCreateTrustScore(userId)
        dao.incrementBorrowed(userId)
    }

    suspend fun onItemReturned(userId: String) {
        getOrCreateTrustScore(userId)
        dao.incrementReturned(userId)
    }

    suspend fun liftExpiredRestrictions(userId: String) {
        dao.liftExpiredRestrictions(userId)
    }

    suspend fun isRestricted(userId: String): Boolean {
        val score = dao.getTrustScore(userId) ?: return false
        if (score.isRestricted && score.restrictedUntil != null) {
            if (score.restrictedUntil < System.currentTimeMillis()) {
                dao.liftExpiredRestrictions(userId)
                return false
            }
        }
        return score.isRestricted
    }
}