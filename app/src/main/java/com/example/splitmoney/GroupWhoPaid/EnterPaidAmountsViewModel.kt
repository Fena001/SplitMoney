package com.example.splitmoney.GroupWhoPaid

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EnterPaidAmountsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun saveMultiplePayers(
        groupId: String,
        expenseId: String,
        totalAmount: Double,
        paymentsMap: Map<String, Double>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val expenseRef = db.collection("groups")
            .document(groupId)
            .collection("expenses")
            .document(expenseId)

        val paymentsRef = expenseRef.collection("payments")
        val batch = db.batch()

        batch.set(expenseRef, mapOf(
            "paidById" to "multiple",
            "paidAmount" to totalAmount
        ), SetOptions.merge())

        paymentsMap.forEach { (userId, amount) ->
            val userPaymentDoc = paymentsRef.document(userId)
            batch.set(userPaymentDoc, mapOf(
                "payerId" to userId,
                "amount" to amount
            ))
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

}
