package com.example.lendloop.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BorrowDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: Int): Person?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories ORDER BY label ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertRecord(record: BorrowRecord): Long

    @Update
    suspend fun updateRecord(record: BorrowRecord)

    @Delete
    suspend fun deleteRecord(record: BorrowRecord)

    @Query("SELECT * FROM borrow_records WHERE status = 'ACTIVE' ORDER BY lentAt DESC")
    fun getActiveRecords(): Flow<List<BorrowRecord>>

    @Query("SELECT * FROM borrow_records WHERE status = 'RETURNED' ORDER BY returnedAt DESC")
    fun getReturnedRecords(): Flow<List<BorrowRecord>>

    // ✅ NEW — used by DashboardViewModel for charts and recent activity
    @Query("SELECT * FROM borrow_records ORDER BY lentAt DESC")
    fun getAllRecords(): Flow<List<BorrowRecord>>

    @Query("SELECT * FROM borrow_records WHERE personId = :personId ORDER BY lentAt DESC")
    fun getRecordsByPerson(personId: Int): Flow<List<BorrowRecord>>

    @Query("SELECT * FROM borrow_records WHERE id = :id")
    suspend fun getRecordById(id: Int): BorrowRecord?

    @Query("""
        UPDATE borrow_records 
        SET status = 'RETURNED', returnedAt = :returnedAt 
        WHERE id = :id
    """)
    suspend fun markReturned(id: Int, returnedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE borrow_records 
        SET lastRemindedAt = :time 
        WHERE id = :id
    """)
    suspend fun updateLastReminded(id: Int, time: Long = System.currentTimeMillis())

    @Query("""
        SELECT * FROM borrow_records 
        WHERE status = 'ACTIVE' 
        AND dueDate IS NOT NULL 
        AND dueDate < :now
    """)
    suspend fun getOverdueRecords(now: Long = System.currentTimeMillis()): List<BorrowRecord>
}