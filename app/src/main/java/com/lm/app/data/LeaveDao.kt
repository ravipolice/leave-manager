package com.lm.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leave_balances WHERE kgid = :kgid")
    suspend fun getBalance(kgid: String): LeaveBalance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: LeaveBalance)

    @Query("SELECT * FROM leave_entries WHERE kgid = :kgid ORDER BY dateFrom DESC")
    fun getAllEntries(kgid: String): Flow<List<LeaveEntry>>

    @Insert
    suspend fun insertEntry(entry: LeaveEntry)

    @Update
    suspend fun updateEntry(entry: LeaveEntry)

    @Delete
    suspend fun deleteEntry(entry: LeaveEntry)

    @Query("SELECT * FROM leave_entries WHERE kgid = :kgid AND leaveType = 'WO' AND year = :year AND month = :month")
    suspend fun getWoEntries(kgid: String, year: Int, month: Int): List<LeaveEntry>

    @Transaction
    suspend fun saveEntryWithBalance(balance: LeaveBalance, entry: LeaveEntry) {
        insertBalance(balance)
        if (entry.id == 0L) {
            insertEntry(entry)
        } else {
            updateEntry(entry)
        }
    }

    @Transaction
    suspend fun deleteEntryWithBalance(balance: LeaveBalance, entry: LeaveEntry) {
        insertBalance(balance)
        deleteEntry(entry)
    }
}
