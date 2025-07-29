package com.example.splitmoney.friendIndividualhome

import User
import com.google.firebase.database.FirebaseDatabase

class FriendRepository {
    fun addFriend(currentUserId: String, friend: User) {
        val db = FirebaseDatabase.getInstance().reference

        // Add each other as friends
        db.child("users").child(currentUserId).child("friends").child(friend.uid).setValue(true)
        db.child("users").child(friend.uid).child("friends").child(currentUserId).setValue(true)
    }

}