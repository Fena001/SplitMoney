package com.example.splitmoney.GroupWhoPaid

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class WhoPaidViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun saveSinglePayer(
        groupId: String,
        expenseId: String,
        userId: String,
        totalAmount: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val expenseRef = db.collection("groups")
            .document(groupId)
            .collection("expenses")
            .document(expenseId)

        // Use set instead of update to avoid "document does not exist" error
        expenseRef.set(
            mapOf(
                "paidById" to userId,
                "paidAmount" to totalAmount,
                "expenseId" to expenseId,
                "groupId" to groupId,
                "timestamp" to System.currentTimeMillis()
            )
        ).addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
