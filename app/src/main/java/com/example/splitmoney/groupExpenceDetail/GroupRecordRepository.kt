package com.example.splitmoney.groupExpenceDetail

import com.example.splitmoney.dataclass.Expense
import com.example.splitmoney.dataclass.IndividualBalance
import com.example.splitmoney.dataclass.ExpenseItem

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import kotlinx.coroutines.tasks.await

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GroupRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUserId = auth.currentUser?.uid.orEmpty()

    suspend fun getGroupBalances(groupId: String): Pair<Double, List<IndividualBalance>> {
        val balancesRef = firestore.collection("groups")
            .document(groupId)
            .collection("balances")

        val snapshot = balancesRef.get().await()
        var overall = 0.0
        val individuals = mutableListOf<IndividualBalance>()

        for (doc in snapshot.documents) {
            val user1 = doc.getString("userId1") ?: continue
            val user2 = doc.getString("userId2") ?: continue
            val amount = doc.getDouble("amount") ?: 0.0

            if (user1 == currentUserId || user2 == currentUserId) {
                val otherUser = if (user1 == currentUserId) user2 else user1
                val adjustedAmount = if (user1 == currentUserId) amount else -amount

                overall += adjustedAmount

                val userName = getUserName(otherUser)
                individuals.add(IndividualBalance(otherUser, userName, adjustedAmount))
            }
        }

        return Pair(overall, individuals)
    }

    suspend fun getExpenses(groupId: String): List<ExpenseItem> {
        val expensesSnapshot = firestore.collection("groups")
            .document(groupId)
            .collection("expenses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return expensesSnapshot.map { doc ->
            val expense = doc.toObject(Expense::class.java)

            val userShare = expense.splitBetween[currentUserId]?.toDouble() ?: 0.0
            val isUserOwed = expense.paidBy.containsKey(currentUserId)

            val payerInfo = when {
                expense.paidBy.size == 1 -> {
                    val payerId = expense.paidBy.keys.first()
                    val payerName = getUserName(payerId)
                    val payerAmount = expense.paidBy[payerId] ?: 0.0
                    "$payerName paid ₹%.2f".format(payerAmount)
                }
                expense.paidBy.size > 1 -> {
                    val totalPaid = expense.paidBy.values.sum()
                    "${expense.paidBy.size} people paid ₹%.2f".format(totalPaid)
                }
                else -> ""
            }

            ExpenseItem(
                expenseId = expense.expenseId,
                date = formatDate(expense.timestamp),
                description = expense.title,
                payerInfo = payerInfo,
                userShare = userShare,
                isUserOwed = isUserOwed
            )
        }
    }


    private suspend fun getUserName(uid: String): String {
        val doc = firestore.collection("users").document(uid).get().await()
        return doc.getString("name") ?: "Unknown"
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}