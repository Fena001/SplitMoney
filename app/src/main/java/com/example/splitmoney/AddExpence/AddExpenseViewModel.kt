package com.example.splitmoney.AddExpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AddExpenseViewModel(groupId: String) : ViewModel() {

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName

    private val _groupType = MutableStateFlow("")
    val groupType: StateFlow<String> = _groupType

    init {
        FirebaseDatabase.getInstance().getReference("groups").child(groupId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _groupName.value = snapshot.child("name").getValue(String::class.java) ?: ""
                    _groupType.value = snapshot.child("type").getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

class AddExpenseViewModelFactory(private val groupId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddExpenseViewModel(groupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
