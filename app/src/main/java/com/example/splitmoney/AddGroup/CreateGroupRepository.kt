package com.example.splitmoney.AddGroup



import Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class CreateGroupRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun createGroup(
        name: String,
        type: String,
        imageUrl: String = "",
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return

        val groupId = UUID.randomUUID().toString()

        val group = Group(
            groupId = groupId,
            name = name,
            type = type,
            members = mapOf(currentUser.uid to true)
        )

        database.child("groups").child(groupId)
            .setValue(group)
            .addOnSuccessListener {
                database.child("users").child(currentUser.uid)
                    .child("groups").child(groupId).setValue(true)
                    .addOnSuccessListener {
                        onSuccess(groupId) // ✅ return groupId here
                    }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }

}
