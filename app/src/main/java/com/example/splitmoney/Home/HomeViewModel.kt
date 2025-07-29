package com.example.splitmoney.Home

import Friend
import Group
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.Group
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.State

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference


    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups


    private val _userName = mutableStateOf("")
    val userName: State<String> = _userName

    // Call this to fetch username from Firebase
    fun fetchUserName(uid: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.get().addOnSuccessListener { snapshot ->
            val name = snapshot.child("name").getValue(String::class.java)
            _userName.value = name ?: ""
        }.addOnFailureListener {
            Log.e("HomeViewModel", "Failed to fetch user name", it)
        }
    }

    fun fetchFriends() {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newFriends = mutableListOf<Friend>()
                    for (child in snapshot.children) {
                        val friendUid = child.key ?: continue
                        database.child("users").child(friendUid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(friendSnapshot: DataSnapshot) {
                                    val name = friendSnapshot.child("name").getValue(String::class.java) ?: return
                                    val email = friendSnapshot.child("email").getValue(String::class.java) ?: return
                                    newFriends.add(Friend(uid = friendUid, name = name, email = email))
                                    _friends.value = newFriends.toList()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Friend load cancelled: ${error.message}")
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Friend list load error: ${error.message}")
                }
            })
    }

    fun fetchGroups() {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("groups")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newGroups = mutableListOf<Group>()
                    for (child in snapshot.children) {
                        val groupId = child.key ?: continue
                        database.child("groups").child(groupId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(groupSnapshot: DataSnapshot) {
                                    val name = groupSnapshot.child("name").getValue(String::class.java) ?: return
                                    val type = groupSnapshot.child("type").getValue(String::class.java) ?: ""
                                    val imageUrl = groupSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                                    val membersMap = groupSnapshot.child("members").children.associate {
                                        it.key!! to (it.getValue(Boolean::class.java) ?: true)
                                    }

                                    newGroups.add(
                                        Group(
                                            groupId = groupId,
                                            name = name,
                                            type = type,
                                            members = membersMap
                                        )
                                    )
                                    _groups.value = newGroups.toList()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Group load cancelled: ${error.message}")
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Group list load error: ${error.message}")
                }
            })
    }

}
