package com.example.splitmoney.friendIndividualhome

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FriendDetailViewModelFactory(
    private val repository: FriendRepository,
    private val friendUid: String,
    private val rawFriendName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendDetailViewModel::class.java)) {
            return FriendDetailViewModel(repository, friendUid, rawFriendName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
