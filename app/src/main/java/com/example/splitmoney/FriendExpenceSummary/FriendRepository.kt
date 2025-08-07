package com.example.splitmoney.FriendExpenceSummary

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import com.example.splitmoney.dataclass.Expense
import com.example.splitmoney.dataclass.ExpenseItem
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.text.SimpleDateFormat
import java.util.*

class FriendRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUserId = auth.currentUser?.uid.orEmpty()

    suspend fun getFriendExpenses(friendUid: String): Pair<Double, List<ExpenseItem>> {
        val allExpensesSnapshot = firestore.collection("expenses")
            .whereArrayContains("participants", currentUserId)
            .get().await()

        var netBalance = 0.0
        val expenseItems = mutableListOf<ExpenseItem>()

        for (doc in allExpensesSnapshot.documents) {
            val expense = doc.toObject(Expense::class.java) ?: continue

            val isRelatedToFriend = expense.splitBetween.containsKey(friendUid) ||
                    expense.paidBy.containsKey(friendUid)
            if (!isRelatedToFriend) continue

            val myShare = expense.splitBetween[currentUserId]?.toDouble() ?: 0.0
            val didIPay = expense.paidBy.containsKey(currentUserId)

            val payerInfo = when {
                expense.paidBy.size == 1 -> {
                    val payerId = expense.paidBy.keys.first()
                    val payerName = getUserName(payerId)
                    "$payerName paid ₹%.2f".format(expense.paidBy[payerId])
                }
                expense.paidBy.size > 1 -> {
                    "${expense.paidBy.size} people paid ₹%.2f".format(expense.paidBy.values.sum())
                }
                else -> ""
            }

            val isUserOwed = expense.paidBy.containsKey(currentUserId)

            if (isUserOwed) netBalance += myShare else netBalance -= myShare

            expenseItems.add(
                ExpenseItem(
                    expenseId = expense.expenseId,
                    date = formatDate(expense.timestamp),
                    description = expense.title,
                    payerInfo = payerInfo,
                    userShare = myShare,
                    isUserOwed = isUserOwed
                )
            )
        }

        return Pair(netBalance, expenseItems)
    }

    private suspend fun getUserName(uid: String): String {
        val doc = firestore.collection("users").document(uid).get().await()
        return doc.getString("name") ?: "Unknown"
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

//    suspend fun getUserNamesByUids(uids: Set<String>): Map<String, String> {
//        val userRef = FirebaseDatabase.getInstance().getReference("users")
//        val result = mutableMapOf<String, String>()
//
//        val deferreds = uids.map { uid ->
//            CoroutineScope(Dispatchers.IO).async {
//                val snapshot = userRef.child(uid).get().await()
//                val name = snapshot.child("name").getValue(String::class.java)
//                if (!name.isNullOrBlank()) result[uid] = name
//            }
//        }
//
//        deferreds.awaitAll()
//        return result
//    }

}