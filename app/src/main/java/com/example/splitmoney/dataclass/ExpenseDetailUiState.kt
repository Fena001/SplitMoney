package com.example.splitmoney.dataclass

data class ExpenseSummaryUiState(
    val title: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "",
    val splitType: String = "Equally",
    val splitDetails: Map<String, Double> = emptyMap()  // name → amount
)


