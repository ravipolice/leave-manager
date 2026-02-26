package com.lm.app.data

data class LeaveStatistics(
    val totalTaken: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val mostUsedType: String = "",
    val averagePerMonth: Double = 0.0,
    val utilizationPercentage: Float = 0f,
    val monthlyBreakdown: Map<Int, Double> = emptyMap(),
    val leaveTypeBreakdown: Map<String, Double> = emptyMap()
)
