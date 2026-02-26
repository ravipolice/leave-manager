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
    suspend fun getLeaveBalance(kgid: String): LeaveBalance {
        return leaveDao.getBalance(kgid) ?: LeaveBalance(kgid = kgid)
    }

    suspend fun saveLeaveBalance(balance: LeaveBalance) {
        leaveDao.insertBalance(balance)
    }

    suspend fun saveLeaveEntry(balance: LeaveBalance, entry: LeaveEntry) {
        leaveDao.saveEntryWithBalance(balance, entry)
    }

    suspend fun deleteLeaveEntry(balance: LeaveBalance, entry: LeaveEntry) {
        leaveDao.deleteEntryWithBalance(balance, entry)
    }

    suspend fun updateLeaveEntry(oldEntry: LeaveEntry, newEntry: LeaveEntry, currentBalance: LeaveBalance): LeaveBalance {
        // Reverse the old entry's effect on the balance
        val reversedBalance = LeaveBalanceCalculator.reverseLeave(currentBalance, oldEntry)
        // Apply the new entry's effect
        val updatedBalance = LeaveBalanceCalculator.applyLeave(reversedBalance, newEntry)
        // Save both (newEntry has the same id so it updates in-place)
        leaveDao.saveEntryWithBalance(updatedBalance, newEntry)
        return updatedBalance
    }

    fun getLeaveEntries(kgid: String): Flow<List<LeaveEntry>> {
        return leaveDao.getAllEntries(kgid)
    }

    suspend fun getWoCountForMonth(kgid: String, year: Int, month: Int): Int {
        return leaveDao.getWoEntries(kgid, year, month).size
    }

    suspend fun getLeaveStatistics(kgid: String, year: Int): LeaveStatistics {
        val balance = getLeaveBalance(kgid)
        val entries = getLeaveEntries(kgid).first().filter { it.year == year }

        // Use Double for summation to handle half-days and HPL (0.5 increments)
        val totalTaken = entries.filter { it.elEntryType != "upcoming" }.sumOf { it.totalDays }
        val totalRemaining = balance.clRemaining + balance.elBalance + balance.hplBalance

        val typeBreakdown = entries
            .filter { it.elEntryType != "upcoming" }
            .groupBy { it.leaveType }
            .mapValues { (_, list) -> list.sumOf { it.totalDays } }

        val mostUsedType = typeBreakdown.maxByOrNull { it.value }?.key ?: ""

        val monthlyBreakdown = entries
            .filter { it.elEntryType != "upcoming" }
            .groupBy { it.month }
            .mapValues { (_, list) -> list.sumOf { it.totalDays } }

        val averagePerMonth = if (entries.isNotEmpty()) {
            totalTaken / monthlyBreakdown.keys.size.coerceAtLeast(1)
        } else 0.0

        val totalAvailable = (balance.clAnnualLimit.toDouble() + balance.elManualBalance + balance.hplBalance).coerceAtLeast(0.1)
        val utilizationPercentage = if (totalAvailable > 0) {
            (totalTaken.toFloat() / totalAvailable.toFloat()) * 100f
        } else 0f

        return LeaveStatistics(
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
