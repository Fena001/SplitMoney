//// FriendExpenseViewModel.kt
//package com.example.splitmoney.FriendAddExpenceScreen
//
//import User
//import androidx.lifecycle.ViewModel
//import com.example.splitmoney.dataclass.Expense
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.FirebaseDatabase
//
//class FriendExpenseViewModel : ViewModel() {
//    // All your existing properties like:
//    var description: String = ""
//    var totalAmount: Float = 0f
//    var whoPaidMap: Map<String, Double> = emptyMap()
//    var splitMap: Map<String, Double> = emptyMap()
//    var participants: List<User> = emptyList()
//
//
//    fun saveExpenseToFirebase(
//        friendUid: String,
//        onSuccess: () -> Unit,
//        onError: (String) -> Unit
//    ) {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        val currentUid = currentUser?.uid ?: run {
//            onError("User not logged in.")
//            return
//        }
//
//        val expense = Expense(
//            expenseId = System.currentTimeMillis().toString(),
//            title = description,
//            amount = totalAmount,
//            paidBy = whoPaidMap.mapValues { it.value.toFloat() },
//            splitBetween = splitMap.mapValues { it.value.toFloat() },
//            timestamp = System.currentTimeMillis()
//        )
//
//        val database = FirebaseDatabase.getInstance().reference
//        val expenseRef = database
//            .child("friend_expenses")
//            .child(currentUid)
//            .child(friendUid)
//            .push()
//
//        expenseRef.setValue(expense)
//            .addOnSuccessListener { onSuccess() }
//            .addOnFailureListener { exception ->
//                onError(exception.message ?: "Unknown error")
//            }
//    }
//}
