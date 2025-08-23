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
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference


    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends


    private val _userName = mutableStateOf("")
    val userName: State<String> = _userName

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    fun fetchGroups(userId: String) {
        val groupsRef = FirebaseDatabase.getInstance().getReference("groups")
        groupsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupList = mutableListOf<Group>()
                snapshot.children.forEach { groupSnapshot ->
                    val members = groupSnapshot.child("members").value as? Map<String, Boolean> ?: return@forEach
                    if (members.containsKey(userId)) {
                        val name = groupSnapshot.child("name").getValue(String::class.java) ?: return@forEach
                        //val icon = groupSnapshot.child("icon").getValue(String::class.java) ?: ""
                        val type = groupSnapshot.child("type").getValue(String::class.java) ?: ""
                       // val netBalance = groupSnapshot.child("balances").child(userId).getValue(Double::class.java)?.toFloat() ?: 0f
                        val otherParty = groupSnapshot.child("otherParty").getValue(String::class.java) ?: ""
                        val icon = groupSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                        val netBalance = groupSnapshot.child("netBalance").getValue(Double::class.java)?.toFloat() ?: 0f


                        groupList.add(
                            Group(
                                groupId = groupSnapshot.key ?: "",
                                name = name,
                                type = type,
                                imageUrl = icon,
                                members = members,
                                netBalance = netBalance,
                                otherParty = otherParty
                            )
                        )
                    }
                }
                _groups.value = groupList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeViewModel", "Failed to fetch groups: ${error.message}")
            }
        })
    }


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
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newFriends = mutableListOf<Friend>()
                    val friendUids = snapshot.children.mapNotNull { it.key }

                    friendUids.forEach { friendUid ->
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

}