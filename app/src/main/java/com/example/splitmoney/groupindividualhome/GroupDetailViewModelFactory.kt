// GroupDetailViewModelFactory.kt
package com.example.splitmoney.groupindividualhome;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

class GroupDetailViewModelFactory(
        private val groupId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupDetailViewModel::class.java)) {
            return GroupDetailViewModel(groupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
