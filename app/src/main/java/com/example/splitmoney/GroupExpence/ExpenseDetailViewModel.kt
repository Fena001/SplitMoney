package com.example.splitmoney.GroupExpence

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.splitmoney.dataclass.Expense
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseDetailViewModel : ViewModel() {
    private val _expense = MutableLiveData<Expense?>()
    val expense: LiveData<Expense?> = _expense

    fun fetchExpense(groupId: String, expenseId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups")
            .document(groupId)
            .collection("expenses")
            .document(expenseId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val expense = doc.toObject(Expense::class.java)
                    _expense.value = expense
                }
            }
            .addOnFailureListener { e ->
                Log.e("ExpenseDetailVM", "Error fetching expense", e)
            }
    }
}
