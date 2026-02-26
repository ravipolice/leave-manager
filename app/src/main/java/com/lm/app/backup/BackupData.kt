package com.lm.app.backup

import com.lm.app.data.LeaveBalance
import com.lm.app.data.LeaveEntry
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val exportDate: Long,
    val balance: LeaveBalance,
    val entries: List<LeaveEntry>
)
