package com.example.splitmoney.dataclass


data class FriendExpenseItem(
    val id: String,
    val title: String,
    val amount: Float,
    val paidBy: String,
    val date: String
)