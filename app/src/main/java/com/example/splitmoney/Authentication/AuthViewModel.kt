package com.example.splitmoney.Authentication

import User
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val loginState = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)

    private val _signUpState = MutableStateFlow<String?>(null)
    val signUpState: StateFlow<String?> = _signUpState

    val userName = mutableStateOf<String?>(null)
    val userEmail = mutableStateOf<String?>(null)

    // ✅ SIGN UP & SAVE USER DATA
    fun registerUser(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val user = User(
                        uid = uid,
                        name = name,
                        email = email
                    )

                    database.reference.child("users").child(uid).setValue(user)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it.message ?: "Database error") }
                } else {
                    onFailure(task.exception?.message ?: "Authentication failed")
                }
            }
    }

    // ✅ LOGIN & FETCH USER INFO
    fun loginUser(email: String, password: String, navController: NavController) {
        isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    database.reference.child("users").child(uid)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val name = snapshot.child("name").getValue(String::class.java)
                            userName.value = name
                            loginState.value = "Success"
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            loginState.value = "Failed to fetch user data"
                        }
                } else {
                    loginState.value = task.exception?.localizedMessage
                }
            }
    }

    // ✅ Fetch user profile info (optional)
    fun fetchUserData(uid: String, onResult: (String?, String?) -> Unit) {
        database.reference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    onResult(name, email)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching user data", error.toException())
                }
            })
    }

    // ✅ For preloading user info in UI
    fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        fetchUserData(uid) { name, email ->
            userName.value = name
            userEmail.value = email
        }
    }
}

