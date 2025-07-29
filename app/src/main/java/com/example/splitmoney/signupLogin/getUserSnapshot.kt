package com.example.splitmoney.signupLogin
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun getUserSnapshot(uid: String): DataSnapshot {
    return suspendCancellableCoroutine { continuation ->
        val ref = FirebaseDatabase.getInstance().getReference("users").child(uid)
        ref.get().addOnSuccessListener {
            continuation.resume(it)
        }.addOnFailureListener {
            continuation.resumeWithException(it)
        }
    }
}
