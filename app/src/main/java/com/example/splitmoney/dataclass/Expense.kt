package com.example.splitmoney.dataclass
data class Expense(
    val expenseId: String = "",
    val title: String = "",
    val amount: Float = 0f,
    val groupId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val splitBetween: Map<String, Double> = emptyMap(), // uid -> share
    val paidBy: Map<String, Double> = emptyMap()        // uid -> amount paid
)


data class Payer(
    val amount: Double = 0.0,
    val name: String = ""
)
