package com.example.splitmoney.FriendAddExpenceScreen

import User
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FriendExpenseViewModel : ViewModel() {

    val currentUser = FirebaseAuth.getInstance().currentUser?.let {
        User(it.uid, it.displayName ?: "You", it.email ?: "")
    } ?: User("anonymous", "You", "unknown")

    var amount: String = ""
    var splitType: String = "equally"

    private val _paidBy = MutableStateFlow<String?>(currentUser.uid)
    val paidBy: StateFlow<String?> = _paidBy

    fun updatePaidBy(payerId: String) {
        _paidBy.value = payerId
    }

    fun updateSplitType(type: String) {
        splitType = type
    }

    fun updateAmount(value: String) {
        amount = value
    }
    fun setPaidBy(payerId: String) {
        _paidBy.value = payerId
    }
    private val _paidAmounts = MutableStateFlow<Map<String, Double>>(emptyMap())
    val paidAmounts: StateFlow<Map<String, Double>> = _paidAmounts

    fun setPaidAmounts(map: Map<String, Double>) {
        _paidAmounts.value = map
    }
}
