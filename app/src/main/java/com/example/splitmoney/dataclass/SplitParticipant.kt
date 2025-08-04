package com.example.splitmoney.dataclass

data class SplitParticipant(
    val userId: String,
    val userName: String,
    val avatarUrl: String?,
    val userBalanceString: String, // "You owe ₹60", "You lent ₹200"
    val isUserOwes: Boolean // true = red, false = green
)
