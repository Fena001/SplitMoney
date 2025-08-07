package com.example.splitmoney.dataclass

import User

data class FriendDetailUiState(
    val expenses: List<Expense> = emptyList(),               // raw data
    val expenseItems: List<ExpenseItem> = emptyList(),       // UI list items
    val currentUser: User = User(),
    val friendUser: User? = null,
    val userNames: Map<String, String> = emptyMap(),
    val netBalance: Double = 0.0                              // optional: if needed in screen
)
