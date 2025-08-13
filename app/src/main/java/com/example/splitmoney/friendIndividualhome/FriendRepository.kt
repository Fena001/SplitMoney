package com.example.splitmoney.friendIndividualhome

import android.util.Log
import com.example.splitmoney.dataclass.Expense
import com.example.splitmoney.dataclass.ExpenseItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class FriendRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUserId = auth.currentUser?.uid.orEmpty()

    // ✅ Reads from Realtime DB (friend_expenses)
    suspend fun getFriendExpensesWithRaw(friendUid: String): Triple<Double, List<ExpenseItem>, List<Expense>> {
        val database = FirebaseDatabase.getInstance().getReference("friend_expenses")
        val result = mutableListOf<Expense>()
        val userFriendExpensesRef = database.child(currentUserId).child(friendUid)
        val snapshot = userFriendExpensesRef.get().await()

        var netBalance = 0.0
        val expenseItems = mutableListOf<ExpenseItem>()

        for (expenseSnap in snapshot.children) {
            val expense = expenseSnap.getValue(Expense::class.java) ?: continue
            result.add(expense)

            val myShare = expense.splitBetween[currentUserId]?.toDouble() ?: 0.0

            val payerInfo = when {
                expense.paidBy.size == 1 -> {
                    val payerId = expense.paidBy.keys.first()
                    val payerName = getUserNameFromRealtime(payerId)
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

        return Triple(netBalance, expenseItems, result)
    }

    // ✅ Fetches a single username from Realtime DB
    private suspend fun getUserNameFromRealtime(uid: String): String {
        val database = FirebaseDatabase.getInstance().getReference("users")
        val snapshot = database.child(uid).get().await()
        val name = snapshot.child("name").getValue(String::class.java)
        return name ?: "Unknown"
    }

    // ✅ Fetches multiple usernames from Realtime DB
    suspend fun getUserNamesByUids(uids: Set<String>): Map<String, String> {
        val database = FirebaseDatabase.getInstance().getReference("users")
        val result = mutableMapOf<String, String>()

        Log.d("REPO", "Fetching names for UIDs: $uids")

        for (uid in uids) {
            try {
                val snapshot = database.child(uid).get().await()
                val name = snapshot.child("name").getValue(String::class.java)

                Log.d("REPO", "UID=$uid, name=$name")

                if (name != null) {
                    result[uid] = name
                } else {
                    result[uid] = "Unknown"
                    Log.w("REPO", "Name is null for uid=$uid")
                }
            } catch (e: Exception) {
                Log.e("REPO", "Error fetching name for uid=$uid: ${e.message}")
                result[uid] = "Unknown"
            }
        }

        Log.d("REPO", "Final uidToNameMap = $result")
        return result
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}