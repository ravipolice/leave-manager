package com.lm.app.utils

import com.lm.app.data.LeaveBalance
import com.lm.app.data.LeaveEntry
import java.util.Calendar

object LeaveBalanceCalculator {

    fun applyLeave(balance: LeaveBalance, entry: LeaveEntry): LeaveBalance {
        return when (entry.leaveType.uppercase()) {
            "CL" -> {
                if (entry.isMcl) {
                    balance.copy(
                        mclUsedThisMonth = balance.mclUsedThisMonth + 1,
                        mclLastUsedMonth = entry.month,
                        mclLastUsedYear = entry.year
                    )
                } else {
                    balance.copy(clRemaining = (balance.clRemaining - entry.totalDays).coerceAtLeast(0.0))
                }
            }
            "MCL" -> {
                balance.copy(
                    mclUsedThisMonth = balance.mclUsedThisMonth + 1,
                    mclLastUsedMonth = entry.month,
                    mclLastUsedYear = entry.year
                )
            }
            "EL" -> {
                if (entry.elEntryType == "taken") {
                    balance.copy(elBalance = (balance.elBalance - entry.totalDays).coerceAtLeast(0.0))
                } else {
                    balance
                }
            }
            "HPL" -> balance.copy(hplBalance = (balance.hplBalance - entry.totalDays).coerceAtLeast(0.0))
            "CCL" -> balance.copy(cclUsed = balance.cclUsed + entry.totalDays)
            "ML" -> balance.copy(maternityUsedCount = balance.maternityUsedCount + 1)
            "PL" -> balance.copy(paternityUsedCount = balance.paternityUsedCount + 1)
            else -> balance
        }
    }

    fun reverseLeave(balance: LeaveBalance, entry: LeaveEntry): LeaveBalance {
        return when (entry.leaveType.uppercase()) {
            "CL" -> {
                if (entry.isMcl) {
                    balance.copy(
                        mclUsedThisMonth = (balance.mclUsedThisMonth - 1).coerceAtLeast(0)
                    )
                } else {
                    val newRemaining = (balance.clRemaining + entry.totalDays)
                        .coerceAtMost(balance.clAnnualLimit.toDouble())
                    balance.copy(clRemaining = newRemaining)
                }
            }
            "MCL" -> {
                balance.copy(
                    mclUsedThisMonth = (balance.mclUsedThisMonth - 1).coerceAtLeast(0)
                )
            }
            "EL" -> {
                if (entry.elEntryType == "taken") {
                    balance.copy(elBalance = balance.elBalance + entry.totalDays)
                } else {
                    balance
                }
            }
            "HPL" -> balance.copy(hplBalance = balance.hplBalance + entry.totalDays)
            "CCL" -> balance.copy(cclUsed = (balance.cclUsed - entry.totalDays).coerceAtLeast(0.0))
            "ML" -> balance.copy(maternityUsedCount = (balance.maternityUsedCount - 1).coerceAtLeast(0))
            "PL" -> balance.copy(paternityUsedCount = (balance.paternityUsedCount - 1).coerceAtLeast(0))
            else -> balance
        }
    }

    fun updateElManualBalance(balance: LeaveBalance, newManualBalance: Double, totalElTaken: Double): LeaveBalance {
        val newElBalance = (newManualBalance - totalElTaken).coerceAtLeast(0.0)
        return balance.copy(
            elManualBalance = newManualBalance,
            elBalance = newElBalance
        )
    }

    fun updateClLimit(balance: LeaveBalance, newLimit: Int, totalClTaken: Double): LeaveBalance {
        val newRemaining = (newLimit.toDouble() - totalClTaken).coerceAtLeast(0.0)
        return balance.copy(
            clAnnualLimit = newLimit,
            clRemaining = newRemaining
        )
    }
}
