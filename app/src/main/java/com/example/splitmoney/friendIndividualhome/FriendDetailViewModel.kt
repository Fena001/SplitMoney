package com.example.splitmoney.friendIndividualhome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.example.splitmoney.dataclass.GroupDetailUiState
import com.example.splitmoney.dataclass.IndividualBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FriendDetailViewModel(
    private val repository: FriendRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState()) // Reused!
    val uiState: StateFlow<GroupDetailUiState> = _uiState

    private val friendUid = savedStateHandle.get<String>("friendUid") ?: ""
    private val friendName = savedStateHandle.get<String>("friendName") ?: ""

    init {
        loadFriendDetail()
    }

    private fun loadFriendDetail() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isLoading = true, groupName = friendName, groupType = "Friend")
                }

                val (netBalance, expenses) = repository.getFriendExpenses(friendUid)

                _uiState.update {
                    it.copy(
                        overallBalance = netBalance,
                        individualBalances = listOf(IndividualBalance(friendUid, friendName, netBalance)),
                        expenses = expenses,
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
