package com.example.splitmoney.AddExpence

import User
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExpenseFlowViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ------------------- Firebase Current User -------------------
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser?.let {
        User(
            uid = it.uid,
            name = it.displayName ?: "You",
            email = it.email ?: "",
            phoneNumber = it.phoneNumber ?: "",
            friends = emptyMap(),
            groups = emptyMap()
        )
    } ?: User(
        uid = "unknown",
        name = "You",
        email = "",
        phoneNumber = "",
        friends = emptyMap(),
        groups = emptyMap()
    )

    // ------------------- Split Type -------------------
    var splitType by mutableStateOf("equally")
        private set

    fun updateSplitType(type: String) {
        splitType = type
    }

    // ------------------- Amount and Description -------------------
    var amountState = mutableStateOf(savedStateHandle["amount"] ?: "")
        private set

    var amount: String
        get() = amountState.value
        set(value) {
            amountState.value = value
            savedStateHandle["amount"] = value
        }

    var title: String
        get() = savedStateHandle["title"] ?: ""
        set(value) {
            savedStateHandle["title"] = value
        }

    var note: String
        get() = savedStateHandle["note"] ?: ""
        set(value) {
            savedStateHandle["note"] = value
        }

    var expenseId: String
        get() = savedStateHandle["expenseId"] ?: System.currentTimeMillis().toString()
        set(value) {
            savedStateHandle["expenseId"] = value
        }

    // ------------------- Selected Members -------------------
    private val _selectedMembers = MutableStateFlow<List<User>>(emptyList())
    val selectedMembers: StateFlow<List<User>> = _selectedMembers

    fun setSelectedMembers(members: List<User>) {
        val updatedMembers = members.toMutableList()
        if (updatedMembers.none { it.uid == currentUser.uid }) {
            updatedMembers.add(currentUser)
        }
        _selectedMembers.value = updatedMembers
    }


    // ------------------- Paid By -------------------
    private val _paidBy = MutableStateFlow<String?>(null) // UID or "multiple"
    val paidBy: StateFlow<String?> = _paidBy

    fun setPaidBy(uid: String) {
        _paidBy.value = uid
    }

    private val _whoPaidMap = MutableStateFlow<Map<String, Double>>(emptyMap())
    val whoPaidMap: StateFlow<Map<String, Double>> = _whoPaidMap

    fun setWhoPaidMap(map: Map<String, Double>) {
        _whoPaidMap.value = map
    }

    fun setPaidAmounts(map: Map<String, Double>) {
        _whoPaidMap.value = map
        _paidBy.value = "multiple"
    }

    fun setWhoPaid(uid: String, map: Map<String, Double>) {
        _paidBy.value = uid
        _whoPaidMap.value = map
    }

    // ------------------- Total and Split Map -------------------
    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    fun setTotalAmount(amount: Double) {
        _totalAmount.value = amount
    }

    private val _splitMap = MutableStateFlow<Map<String, Double>>(emptyMap())
    val splitMap: StateFlow<Map<String, Double>> = _splitMap

    fun setSplitMap(map: Map<String, Double>) {
        _splitMap.value = map
    }

    private val _splitBetweenMap = MutableStateFlow<Map<String, Double>>(emptyMap())
    val splitBetweenMap: StateFlow<Map<String, Double>> = _splitBetweenMap

    fun setSplitBetweenMap(map: Map<String, Double>) {
        _splitBetweenMap.value = map
    }

    // ------------------- Reset -------------------
    fun reset() {
        _totalAmount.value = 0.0
        _splitMap.value = emptyMap()
        _splitBetweenMap.value = emptyMap()
        _paidBy.value = null
        _whoPaidMap.value = emptyMap()
        amount = ""
        title = ""
        note = ""
        splitType = "equally"
        // Don't reset selected members unless explicitly needed
    }
}
