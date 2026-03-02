package com.lm.app.utils

import com.lm.app.data.User
import com.lm.app.data.LeaveBalance
import com.lm.app.data.LeaveEntry

object LeaveValidationEngine {

    fun validateLeave(
        user: User,
        balance: LeaveBalance,
        entry: LeaveEntry,
        woCountThisMonth: Int
    ): Result<Unit> {

        // Since this app is for personal tracking, we allow the user to save entries even if they violate rules.
        // The UI handles showing informative warnings dynamically.
        // We only block structurally impossible things if needed, but for now we just allow it.

        if (entry.leaveType.uppercase() == "CL") {
            if (entry.isHalfDay && entry.totalDays != 0.5) {
                return Result.failure(Exception("Half-day CL must be exactly 0.5 days"))
            }
        }

        return Result.success(Unit)
    }

    private fun checkGenderCompatibility(gender: String, leaveType: String, isMcl: Boolean): String? {
        val g = gender.lowercase()
        val type = leaveType.uppercase()

        return when {
            isMcl || type == "MCL" -> if (g != "female") "MCL is only available for female employees" else null
            type == "ML" || type == "CCL" -> if (g != "female") "Leave type not allowed for selected gender" else null
            type == "PL" -> if (g != "male") "Leave type not allowed for selected gender" else null
            else -> null
        }
    }
}
