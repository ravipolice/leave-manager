package com.lm.app.data

import com.lm.app.utils.LeaveBalanceCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar

@Singleton
class LeaveRepository @Inject constructor(
    private val leaveDao: LeaveDao
) {
    suspend fun getLeaveBalance(kgid: String): LeaveBalance = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        leaveDao.getBalance(kgid) ?: LeaveBalance(kgid = kgid)
    }

    suspend fun saveLeaveBalance(balance: LeaveBalance) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        leaveDao.insertBalance(balance)
    }

    suspend fun saveLeaveEntry(balance: LeaveBalance, entry: LeaveEntry) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        leaveDao.saveEntryWithBalance(balance, entry)
    }

    suspend fun deleteLeaveEntry(balance: LeaveBalance, entry: LeaveEntry) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        leaveDao.deleteEntryWithBalance(balance, entry)
    }

    suspend fun updateLeaveEntry(oldEntry: LeaveEntry, newEntry: LeaveEntry, currentBalance: LeaveBalance): LeaveBalance = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        // Reverse the old entry's effect on the balance
        val reversedBalance = LeaveBalanceCalculator.reverseLeave(currentBalance, oldEntry)
        // Apply the new entry's effect
        val updatedBalance = LeaveBalanceCalculator.applyLeave(reversedBalance, newEntry)
        // Save both (newEntry has the same id so it updates in-place)
        leaveDao.saveEntryWithBalance(updatedBalance, newEntry)
        updatedBalance
    }

    fun getLeaveEntries(kgid: String): Flow<List<LeaveEntry>> {
        return leaveDao.getAllEntries(kgid)
    }

    suspend fun getWoCountForMonth(kgid: String, year: Int, month: Int): Int {
        return leaveDao.getWoEntries(kgid, year, month).size
    }

    suspend fun getLeaveStatistics(kgid: String, year: Int): LeaveStatistics = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val balance = getLeaveBalance(kgid)
        val entries = getLeaveEntries(kgid).first().filter { it.year == year }
        
        // ... (rest of stats logic stays same, just wrapped) ...
        val totalTaken = entries.filter { it.elEntryType == "taken" }.sumOf { it.totalDays }
        val totalRemaining = balance.clRemaining + balance.elBalance + balance.hplBalance

        val typeBreakdown = entries
            .filter { it.elEntryType == "taken" }
            .groupBy { it.leaveType }
            .mapValues { (_, list) -> list.sumOf { it.totalDays } }

        val mostUsedType = typeBreakdown.maxByOrNull { it.value }?.key ?: ""

        val monthlyBreakdown = entries
            .filter { it.elEntryType == "taken" }
            .groupBy { it.month }
            .mapValues { (_, list) -> list.sumOf { it.totalDays } }

        val averagePerMonth = if (entries.isNotEmpty()) {
            totalTaken / monthlyBreakdown.keys.size.coerceAtLeast(1)
        } else 0.0

        val totalAvailable = (balance.clAnnualLimit.toDouble() + balance.elManualBalance + balance.hplBalance).coerceAtLeast(0.1)
        val utilizationPercentage = if (totalAvailable > 0) {
            (totalTaken.toFloat() / totalAvailable.toFloat()) * 100f
        } else 0f

        LeaveStatistics(
            totalTaken = totalTaken,
            totalRemaining = totalRemaining,
            mostUsedType = mostUsedType,
            averagePerMonth = averagePerMonth,
            utilizationPercentage = utilizationPercentage,
            monthlyBreakdown = monthlyBreakdown,
            leaveTypeBreakdown = typeBreakdown
        )
    }
}
