package com.example.lendloop.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(trustScore: TrustScore)

    @Query("SELECT * FROM trust_scores WHERE userId = :userId")
    suspend fun getTrustScore(userId: String): TrustScore?

    @Query("SELECT * FROM trust_scores WHERE userId = :userId")
    fun observeTrustScore(userId: String): Flow<TrustScore?>

    @Query("""
        UPDATE trust_scores 
        SET totalBorrowed = totalBorrowed + 1,
            lastUpdated = :now
        WHERE userId = :userId
    """)
    suspend fun incrementBorrowed(userId: String, now: Long = System.currentTimeMillis())

    @Query("""
        UPDATE trust_scores
        SET totalReturned = totalReturned + 1,
            returnRate = CAST(totalReturned + 1 AS FLOAT) / CAST(totalBorrowed AS FLOAT) * 100,
            isRestricted = CASE 
                WHEN CAST(totalReturned + 1 AS FLOAT) / CAST(totalBorrowed AS FLOAT) * 100 < 50 
                THEN 1 ELSE 0 END,
            restrictedUntil = CASE 
                WHEN CAST(totalReturned + 1 AS FLOAT) / CAST(totalBorrowed AS FLOAT) * 100 < 50 
                THEN :restrictedUntil ELSE NULL END,
            lastUpdated = :now
        WHERE userId = :userId
    """)
    suspend fun incrementReturned(
        userId: String,
        restrictedUntil: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
        now: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE trust_scores
        SET isRestricted = 0, restrictedUntil = NULL
        WHERE userId = :userId AND restrictedUntil < :now
    """)
    suspend fun liftExpiredRestrictions(userId: String, now: Long = System.currentTimeMillis())
}
