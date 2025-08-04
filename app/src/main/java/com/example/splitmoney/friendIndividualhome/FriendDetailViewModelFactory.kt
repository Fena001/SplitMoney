package com.example.splitmoney.friendIndividualhome

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FriendDetailViewModelFactory(
    private val repository: FriendRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FriendDetailViewModel(repository, savedStateHandle) as T
    }
}
