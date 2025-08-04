package com.example.splitmoney.dataclass

data class MonthlySpending(
    val month: String,
    val amount: Double,
    val trendBarPercentage: Float // 0.0 to 1.0
)
