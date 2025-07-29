package com.example.splitmoney.friendContact

import User
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class ContactPickerViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    init {
        fetchAllUsers()
    }

    private fun fetchAllUsers() {
        database.child("users").get().addOnSuccessListener { snapshot ->
            val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
            _allUsers.value = users
        }
    }

    fun addFriend(friendUid: String, onSuccess: () -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                database.child("users").child(currentUserId).child("friends").child(friendUid).setValue(true).await()
                database.child("users").child(friendUid).child("friends").child(currentUserId).setValue(true).await()

                // Switch to Main thread for UI callback
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
