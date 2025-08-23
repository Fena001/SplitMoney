// BalanceSummary.kt
package com.example.splitmoney.dataclass

data class BalanceSummary(
    val netBalance: Double = 0.0,                       // +ve = you should receive
    val topDebtsOwedToYou: List<MemberBalance> = emptyList(),
    val topDebtsYouOwe: List<MemberBalance> = emptyList(),
    val otherBalancesCount: Int = 0
)

data class MemberBalance(
    val uid: String,
    val memberName: String,
    val amount: Double                                  // always positive
)
