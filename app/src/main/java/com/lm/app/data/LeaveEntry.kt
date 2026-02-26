package com.lm.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.lm.app.utils.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Entity(tableName = "leave_entries")
@Serializable
data class LeaveEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kgid: String = "",
    @Serializable(with = DateSerializer::class)
    val dateFrom: Date? = null,
    @Serializable(with = DateSerializer::class)
    val dateTo: Date? = null,
    val totalDays: Double = 0.0,
    val leaveType: String = "", // CL, EL, HPL, WO, EOL, ML, PL, CCL, MCL
    val remark: String? = null,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date = Date(),
    @Serializable(with = DateSerializer::class)
    val modifiedAt: Date = Date(),
    val year: Int = 0,
    val month: Int = 0,
    val isHalfDay: Boolean = false,
    val isMcl: Boolean = false,
    val elEntryType: String = "taken" // "taken" | "upcoming"
)
