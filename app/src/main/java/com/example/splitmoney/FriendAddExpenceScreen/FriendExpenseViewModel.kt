package com.example.splitmoney.FriendAddExpenceScreen

import User
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FriendExpenseViewModel : ViewModel() {

    // Current logged-in user
    val currentUser: User = FirebaseAuth.getInstance().currentUser?.let {
        User(it.uid, it.displayName ?: "You", it.email ?: "")
    } ?: User("anonymous", "You", "unknown")

    // Description of the expense
    var description by mutableStateOf("")
        private set

    fun updateDescription(desc: String) {
        description = desc
    }

    // Total amount of the expense
    var amount by mutableStateOf("")
        private set

    fun updateAmount(value: String) {
        amount = value
    }

    // Who paid: single or multiple user IDs
    var paidByUserIds by mutableStateOf(listOf(currentUser.uid))
        private set

    fun setPaidBySingle(userId: String) {
        paidByUserIds = listOf(userId)
    }

    fun setPaidByMultiple(userIds: List<String>) {
        paidByUserIds = userIds
    }

    // Paid amounts in case of multiple payers (e.g., for unequally split)
    private val _paidAmounts = MutableStateFlow<Map<String, Double>>(emptyMap())
    val paidAmounts: StateFlow<Map<String, Double>> = _paidAmounts

    fun setPaidAmounts(map: Map<String, Double>) {
        _paidAmounts.value = map
    }
    private val _splitMap = mutableStateOf<Map<String, Float>>(emptyMap())
    val splitMap: Map<String, Float> get() = _splitMap.value

    fun setSplitMap(map: Map<String, Float>) {
        _splitMap.value = map
    }
    private val _splitType = mutableStateOf("equally")
    val splitType: String get() = _splitType.value

    fun setSplitType(type: String) {
        _splitType.value = type
    }
    private val _paidBy = mutableStateOf("single")
    val paidBy: String get() = _paidBy.value

    fun setPaidBy(value: String) {
        _paidBy.value = value
    }

    private val _whoPaidMap = mutableStateOf<Map<String, Double>>(emptyMap())
    val whoPaidMap: Map<String, Double> get() = _whoPaidMap.value

    fun setWhoPaidMap(map: Map<String, Double>) {
        _whoPaidMap.value = map
    }
}
