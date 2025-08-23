package com.example.splitmoney.GroupExpence

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.splitmoney.dataclass.Expense
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseDetailViewModel : ViewModel() {

    private val _expense = MutableLiveData<Expense?>()
    val expense: LiveData<Expense?> = _expense

    private val _nameMap = MutableLiveData<Map<String, String>>()
    val nameMap: LiveData<Map<String, String>> = _nameMap

    fun fetchExpense(groupId: String, expenseId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("groups")
            .document(groupId)
            .collection("expenses")
            .document(expenseId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val expense = doc.toObject(Expense::class.java) ?: return@addOnSuccessListener

                    Log.d("ExpenseDetailVM", "Raw paidBy: ${expense.paidBy}")
                    Log.d("ExpenseDetailVM", "Raw splitBetween: ${expense.splitBetween}")

                    val userIds = (expense.paidBy.keys + expense.splitBetween.keys).toSet()
                    Log.d("ExpenseDetailVM", "Unique userIds: $userIds")

                    fetchUserNames(userIds) { nameMap ->
                        _nameMap.value = nameMap

                        val updatedPaidBy = expense.paidBy.mapKeys { (uid, _) ->
                            nameMap[uid] ?: uid
                        }
                        val updatedSplitBetween = expense.splitBetween.mapKeys { (uid, _) ->
                            nameMap[uid] ?: uid
                        }

                        Log.d("ExpenseDetailVM", "Updated paidBy: $updatedPaidBy")
                        Log.d("ExpenseDetailVM", "Updated splitBetween: $updatedSplitBetween")

                        _expense.value = expense.copy(
                            paidBy = updatedPaidBy,
                            splitBetween = updatedSplitBetween
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ExpenseDetailVM", "Error fetching expense", e)
            }
    }

    private fun fetchUserNames(userIds: Set<String>, onComplete: (Map<String, String>) -> Unit) {
        if (userIds.isEmpty()) {
            onComplete(emptyMap())
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereIn(FieldPath.documentId(), userIds.toList())
            .get()
            .addOnSuccessListener { snapshot ->
                val nameMap = snapshot.documents.associate { doc ->
                    doc.id to (doc.getString("name") ?: "Unknown")
                }
                onComplete(nameMap)
            }
            .addOnFailureListener {
                Log.e("ExpenseDetailVM", "Error fetching user names", it)
                onComplete(emptyMap())
            }
    }
}
