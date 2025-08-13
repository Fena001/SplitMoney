package com.example.splitmoney.dataclass
data class Expense(
    val expenseId: String = "",
    val title: String = "",
    val amount: Float = 0f,
    val paidBy: Map<String, Float> = emptyMap(), // ⬅️ multiple contributors
    val groupId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val splitBetween: Map<String, Float> = emptyMap() // who owes what
)