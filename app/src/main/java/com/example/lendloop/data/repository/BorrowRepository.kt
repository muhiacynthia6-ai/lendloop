package com.example.lendloop.data.repository

import com.example.lendloop.data.db.BorrowDao
import com.example.lendloop.data.db.BorrowRecord
import com.example.lendloop.data.db.Category
import com.example.lendloop.data.db.Direction
import com.example.lendloop.data.db.Person
import com.example.lendloop.util.SessionManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BorrowRepository @Inject constructor(
    private val dao: BorrowDao,
    private val trustScoreRepository: TrustScoreRepository,
    private val sessionManager: SessionManager
) {
    // --- Person ---
    suspend fun insertPerson(person: Person): Long = dao.insertPerson(person)
    fun getAllPersons(): Flow<List<Person>> = dao.getAllPersons()
    suspend fun getPersonById(id: Int): Person? = dao.getPersonById(id)

    // --- Category ---
    suspend fun insertCategory(category: Category) = dao.insertCategory(category)
    fun getAllCategories(): Flow<List<Category>> = dao.getAllCategories()

    // --- BorrowRecord ---
    suspend fun insertRecord(record: BorrowRecord): Long {
        val id = dao.insertRecord(record)
        // If user is borrowing something, update their trust score
        if (record.direction == Direction.BORROWED) {
            val userId = sessionManager.getUserId()
            if (userId != -1) trustScoreRepository.onItemBorrowed(userId)
        }
        return id
    }

    suspend fun updateRecord(record: BorrowRecord) = dao.updateRecord(record)
    suspend fun deleteRecord(record: BorrowRecord) = dao.deleteRecord(record)
    fun getActiveRecords(): Flow<List<BorrowRecord>> = dao.getActiveRecords()
    fun getReturnedRecords(): Flow<List<BorrowRecord>> = dao.getReturnedRecords()
    fun getRecordsByPerson(personId: Int): Flow<List<BorrowRecord>> =
        dao.getRecordsByPerson(personId)
    suspend fun getRecordById(id: Int): BorrowRecord? = dao.getRecordById(id)

    suspend fun markReturned(id: Int) {
        dao.markReturned(id)
        // Update trust score for the borrower
        val record = dao.getRecordById(id)
        if (record?.direction == Direction.BORROWED) {
            val userId = sessionManager.getUserId()
            if (userId != -1) trustScoreRepository.onItemReturned(userId)
        }
    }

    suspend fun updateLastReminded(id: Int) = dao.updateLastReminded(id)
    suspend fun getOverdueRecords(): List<BorrowRecord> = dao.getOverdueRecords()

    // Trust score check before borrowing
    suspend fun canUserBorrow(): Boolean {
        val userId = sessionManager.getUserId()
        if (userId == -1) return false
        return !trustScoreRepository.isRestricted(userId)
    }
}