package com.example.splitmoney.friendAdjustSplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FriendAdjustSplitViewModelFactory(private val uids: List<String>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FriendAdjustSplitViewModel(uids) as T
    }
}
