package com.example.splitmoney.GroupExpence

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitmoney.dataclass.Expense
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ExpenseDetailViewModel : ViewModel() {
        private val db = FirebaseDatabase.getInstance().reference
        private val _expense = MutableLiveData<Expense?>()
        val expense: LiveData<Expense?> = _expense

        private val _userNameMap = MutableLiveData<Map<String, String>>(emptyMap())
        val userNameMap: LiveData<Map<String, String>> = _userNameMap

        fun fetchExpense(groupId: String, expenseId: String) {
            viewModelScope.launch {
                val expenseSnap = FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(groupId)
                    .collection("expenses")
                    .document(expenseId)
                    .get()
                    .await()

                val expense = expenseSnap.toObject(Expense::class.java)
                _expense.value = expense

                // 🔑 Fetch user names for all UIDs
                val uids = (expense?.paidBy?.keys ?: emptySet()) + (expense?.splitBetween?.keys ?: emptySet())
                val nameMap = mutableMapOf<String, String>()

                for (uid in uids) {
                    val snapshot = db.child("users").child(uid).get().await()
                    val name = snapshot.child("name").getValue(String::class.java) ?: uid
                    nameMap[uid] = name
                }
                _userNameMap.value = nameMap
            }
        }
    }
