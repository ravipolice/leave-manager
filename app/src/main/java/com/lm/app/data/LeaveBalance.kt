package com.lm.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "leave_balances")
@Serializable
data class LeaveBalance(
    @PrimaryKey
    val kgid: String = "",
    val clYear: Int = 0,
    val clAnnualLimit: Int = 15,
    val clRemaining: Double = 15.0,
    val elManualBalance: Double = 0.0,
    val elBalance: Double = 0.0,
    val hplBalance: Double = 0.0,
    val cclUsed: Double = 0.0,
    val maternityUsedCount: Int = 0,
    val paternityUsedCount: Int = 0,
    val mclUsedThisMonth: Int = 0,
    val mclLastUsedMonth: Int = 0,
    val mclLastUsedYear: Int = 0,
    val lastResetYear: Int = 0,
    val lastCreditDate: String = "", // Format: YYYY-MM-DD
    val lastElHplCreditDate: String = "" // Format: YYYY-MM
)
