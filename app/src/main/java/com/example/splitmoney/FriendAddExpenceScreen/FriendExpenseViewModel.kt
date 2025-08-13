package com.example.splitmoney.FriendAddExpenceScreen

import User
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.splitmoney.Calculation.calculateEqualSplit
import com.example.splitmoney.Calculation.isPercentageValid
import com.example.splitmoney.Calculation.isTotalValid
import com.example.splitmoney.Calculation.parsePercentageSplit
import com.example.splitmoney.Calculation.parseUnequalSplit
import com.example.splitmoney.dataclass.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FriendExpenseViewModel : ViewModel() {

    // -----------------------------
    // Firebase Logged-in User
    // -----------------------------
    val currentUser: User = FirebaseAuth.getInstance().currentUser?.let {
        val name = it.displayName?.takeIf { it.isNotBlank() } ?: "You"
        User(it.uid, name, it.email ?: "")
    } ?: User("anonymous", "You", "unknown")

    // -----------------------------
    // Expense Fields
    // -----------------------------
    var description by mutableStateOf("")
        private set

    var amount by mutableStateOf("")
        private set

    fun updateDescription(desc: String) {
        description = desc
        Log.d("FriendExpenseVM", "Description updated: $desc")
    }

    fun updateAmount(value: String) {
        amount = value
        Log.d("FriendExpenseVM", "Amount updated: $value")
    }

    private val _totalAmount = mutableStateOf(0f)
    val totalAmount: Float get() = _totalAmount.value

    fun setTotalAmount(value: Float) {
        _totalAmount.value = value
        Log.d("FriendExpenseVM", "✅ TotalAmount set: $value")
    }

    private val _paidByUser = mutableStateOf<User?>(null)
    val paidByUser: User get() = _paidByUser.value ?: currentUser

    fun setPaidByUser(user: User) {
        _paidByUser.value = user
    }

    private val _paidBy = mutableStateOf("single") // "single" or "multiple"
    val paidBy: String get() = _paidBy.value

    fun setPaidBy(value: String) {
        _paidBy.value = value
    }

    var paidByUserIds by mutableStateOf(listOf(currentUser.uid))
        private set

    fun setPaidBySingle(userId: String) {
        paidByUserIds = listOf(userId)
    }

    fun setPaidByMultiple(userIds: List<String>) {
        paidByUserIds = userIds
    }

    private val _whoPaidMap = mutableStateOf<Map<String, Double>>(emptyMap())
    val whoPaidMap: Map<String, Double> get() = _whoPaidMap.value

    fun setWhoPaidMap(map: Map<String, Double>) {
        _whoPaidMap.value = map
    }

    // -----------------------------
    // Participants
    // -----------------------------
    private val _participants = mutableStateListOf<User>()
    val participants: List<User> get() = _participants

    fun setParticipants(users: List<User>) {
        _participants.clear()
        _participants.addAll(users)
    }

    // -----------------------------
    // Split Data
    // -----------------------------
    private val _splitType = mutableStateOf("Equally")
    val splitType: String get() = _splitType.value

    fun setSplitType(type: String) {
        _splitType.value = type
    }

    private val _splitMap = mutableStateOf<Map<String, Float>>(emptyMap())
    val splitMap: Map<String, Float> get() = _splitMap.value

    fun setSplitMap(map: Map<String, Float>) {
        _splitMap.value = map
    }

    // -----------------------------
    // Save to Firebase
    //  -----------------------------
    fun saveExpenseToFirebase(friendUid: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            onError("User not logged in.")
            return
        }

        val expense = Expense(
            expenseId = System.currentTimeMillis().toString(),
            title = description,
            amount = totalAmount,
            paidBy = whoPaidMap.mapValues { it.value.toFloat() },
            splitBetween = splitMap.mapValues { it.value.toFloat() },
            timestamp = System.currentTimeMillis()
        )

        val dbRef = FirebaseDatabase.getInstance().getReference("friend_expenses")
            .child(currentUid)
            .child(friendUid)
            .child("expenses")
            .push()

        dbRef.setValue(expense)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Unknown error") }
    }

    // -----------------------------
    // Reset All Fields
    // -----------------------------
    fun clearExpenseData() {
        description = ""
        amount = ""
        setTotalAmount(0f)
        setPaidBy("single")
        paidByUserIds = listOf(currentUser.uid)
        setWhoPaidMap(emptyMap())
        setSplitType("Equally")
        setSplitMap(emptyMap())
        _participants.clear()
    }


    fun updateEqualSplit(selectedFriends: Map<String, Boolean>, totalAmount: Double) {
        val calculatedMap = calculateEqualSplit(selectedFriends, totalAmount)
        setSplitMap(calculatedMap.mapValues { it.value.toFloat() })
    }

    fun updateUnequalSplit(userAmounts: Map<String, String>, totalAmount: Double): Boolean {
        val parsed = parseUnequalSplit(userAmounts)
        setSplitMap(parsed.mapValues { it.value.toFloat() })
        return         isTotalValid(userAmounts, totalAmount)
    }

    fun updatePercentageSplit(percentages: Map<String, String>, totalAmount: Double): Boolean {
        val calculated = parsePercentageSplit(percentages, totalAmount)
        setSplitMap(calculated.mapValues { it.value.toFloat() })
        return isPercentageValid(percentages)
    }

}