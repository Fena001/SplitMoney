package com.example.splitmoney.friendAdjustSplit

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import User // Replace with your actual User model import path


class FriendAdjustSplitViewModel(private val uids: List<String>) : ViewModel() {

    private val _participants = MutableStateFlow<List<User>>(emptyList())
    val participants: StateFlow<List<User>> = _participants

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        val db = FirebaseDatabase.getInstance().reference
        val fetchedUsers = mutableListOf<User>()
        var count = 0

        for (uid in uids) {
            db.child("users").child(uid).get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                user?.let { fetchedUsers.add(it) }
                count++
                if (count == uids.size) {
                    _participants.value = fetchedUsers
                }
            }
        }
    }
}
