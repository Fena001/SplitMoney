package com.example.splitmoney.friendIndividualhome

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.example.splitmoney.dataclass.GroupDetailUiState
import com.example.splitmoney.dataclass.IndividualBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.splitmoney.dataclass.Expense

class FriendDetailViewModel(
    private val repository: FriendRepository,
    private val friendUid: String,
    private val rawFriendName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState

    init {
        loadFriendDetail()
        Log.d("VIEWMODEL", "SavedStateHandle friendUid = $friendUid, friendName = $rawFriendName")
    }


    private fun loadFriendDetail() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isLoading = true, groupName = rawFriendName, groupType = "Friend")
                }

                val (netBalance, expenseItems, rawExpenses) = repository.getFriendExpensesWithRaw(friendUid)

                val allUids = (rawExpenses
                    .flatMap { it.paidBy.keys + it.splitBetween.keys } + friendUid).toSet()
                Log.d("VIEWMODEL", "All UIDs used in getUserNamesByUids: $allUids")

                val uidToNameMap = repository.getUserNamesByUids(allUids)

                val resolvedName = uidToNameMap[friendUid]
                    ?.takeIf { it.isNotBlank() }
                    ?: rawFriendName.takeIf { it.isNotBlank() }
                    ?: "Unknown"
                Log.d("VIEWMODEL", "Resolved friendName = $resolvedName")

                _uiState.update {
                    it.copy(
                        overallBalance = netBalance,
                        individualBalances = listOf(IndividualBalance(friendUid, resolvedName, netBalance)),
                        expenses = expenseItems,
                        userNames = uidToNameMap,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to load friend data", isLoading = false)
                }
            }
        }
    }
}
