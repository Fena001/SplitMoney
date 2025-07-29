package com.example.splitmoney.groupindividualhome

import Group
import User
import com.example.splitmoney.dataclass.Expense
import com.google.firebase.database.*

class GroupDetailRepository(
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
) {
    private val database = FirebaseDatabase.getInstance().reference

    fun getGroup(groupId: String, onResult: (Group?) -> Unit) {
        db.child("groups").child(groupId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val group = snapshot.getValue(Group::class.java)
                    onResult(group)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(null)
                }
            })
    }

    fun getGroupExpenses(groupId: String, onResult: (List<Expense>) -> Unit) {
        db.child("groupExpenses").child(groupId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = snapshot.children.mapNotNull {
                        it.getValue(Expense::class.java)
                    }
                    onResult(expenses)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }

    fun getGroupMembers(memberIds: List<String>, onResult: (List<User>) -> Unit) {
        val usersRef = db.child("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (uid in memberIds) {
                    val userSnapshot = snapshot.child(uid)
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) userList.add(user)
                }
                onResult(userList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun updateGroupMembers(groupId: String, updatedMembers: Map<String, Boolean>, onComplete: () -> Unit) {
        val groupRef = db.child("groups").child(groupId)
        groupRef.child("members").setValue(updatedMembers)
            .addOnSuccessListener { onComplete() }
    }
    fun addGroupMembers(
        groupId: String,
        newMemberUids: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updates = mutableMapOf<String, Any?>()

        newMemberUids.forEach { uid ->
            updates["groups/$groupId/members/$uid"] = true
            updates["users/$uid/groups/$groupId"] = true
        }

        database.updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
