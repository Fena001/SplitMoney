package com.example.splitmoney.AddExpence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.splitmoney.AddExpense.AddExpenseViewModel

class AddExpenseViewModelFactory(private val groupId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddExpenseViewModel(groupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}