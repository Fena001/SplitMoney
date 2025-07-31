package com.example.splitmoney.dataclass

data class ExpenseItem(
    val expenseId: String,
    val date: String,
    val description: String,
    val payerInfo: String,
    val userShare: Double,
    val isUserOwed: Boolean
)
