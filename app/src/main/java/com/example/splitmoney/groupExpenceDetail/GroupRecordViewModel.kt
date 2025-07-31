package com.example.splitmoney.groupExpenceDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle

import com.example.splitmoney.dataclass.GroupDetailUiState
import com.example.splitmoney.groupExpenceDetail.GroupRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupDetailViewModel(
    private val repository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState

    private val groupId = savedStateHandle.get<String>("groupId") ?: ""
    private val groupName = savedStateHandle.get<String>("groupName") ?: ""
    private val groupType = savedStateHandle.get<String>("groupType") ?: ""

    init {
        loadGroupDetail()
    }

    private fun loadGroupDetail() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, groupName = groupName, groupType = groupType) }

                val (overallBalance, balances) = repository.getGroupBalances(groupId)
                val expenses = repository.getExpenses(groupId)

                _uiState.update {
                    it.copy(
                        overallBalance = overallBalance,
                        individualBalances = balances,
                        expenses = expenses,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load data", isLoading = false) }
            }
        }
    }
}
