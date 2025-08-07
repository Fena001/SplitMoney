package com.example.splitmoney.dataclass

data class GroupDetailUiState(
    val groupName: String = "",
    val groupType: String = "",
    val overallBalance: Double = 0.0,
    val individualBalances: List<IndividualBalance> = emptyList(),
    val expenses: List<ExpenseItem> = emptyList(),
    val userNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

)
