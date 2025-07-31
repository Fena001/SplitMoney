package com.example.splitmoney.dataclass

data class IndividualBalance(
    val userId: String,
    val userName: String,
    val amount: Double // Positive = they owe you, negative = you owe them
)
