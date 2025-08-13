package com.example.splitmoney.groupindividualhome

import Group
import User
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitmoney.dataclass.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailViewModel(
    private val groupId: String,
    private val repository: GroupDetailRepository = GroupDetailRepository()
) : ViewModel() {

    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members: StateFlow<List<User>> = _members

    private val firebaseRef = FirebaseDatabase.getInstance().reference
    private val database = FirebaseDatabase.getInstance().reference

    init {
        loadGroupExpenses()
        loadGroupMembers()
    }

    fun loadGroupMembers() {
        val groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("members")

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userIds = snapshot.children.mapNotNull { it.key }

                if (userIds.isEmpty()) {
                    _members.value = emptyList()
                    return
                }

                val usersRef = FirebaseDatabase.getInstance().getReference("users")
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        val fetchedUsers = userIds.mapNotNull { uid ->
                            userSnapshot.child(uid).getValue(User::class.java)
                        }
                        _members.value = fetchedUsers
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun loadGroupExpenses() {
        // ❌ Remove this
        // repository.getGroupExpenses(groupId) { expenses ->
        //     _expenses.value = expenses
        // }

        // ✅ Use Firestore instead
        fetchExpensesForGroup(groupId)
    }

    fun fetchMembersFromFirebase(onResult: (List<User>) -> Unit) {
        db.collection("groups").document(groupId).get().addOnSuccessListener { groupSnapshot ->
            val memberIds = (groupSnapshot["members"] as? Map<*, *>)?.keys?.mapNotNull { it as? String } ?: emptyList()

            if (memberIds.isEmpty()) {
                onResult(emptyList())
                return@addOnSuccessListener
            }

            db.collection("users")
                .whereIn("uid", memberIds)
                .get()
                .addOnSuccessListener { userSnapshots ->
                    val members = userSnapshots.mapNotNull { it.toObject(User::class.java) }
                    onResult(members)
                }
        }
    }

    private val db = FirebaseFirestore.getInstance()

    fun fetchExpensesForGroup(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups")
            .document(groupId)
            .collection("expenses")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                _expenses.value = list
            }
    }


}
