package com.example.splitmoney.GroupContacts

import User
import android.app.Application
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectContactsScreen(
    groupId: String,
    onConfirmSelection: (List<User>) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permission required to access contacts", Toast.LENGTH_SHORT).show()
        }
    }
    val viewModel: ContactViewModel = viewModel(
        factory = ContactViewModelFactory(context.applicationContext as Application)
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionState.launch(android.Manifest.permission.READ_CONTACTS)
        } else {
            viewModel.loadContacts()
        }
    }

//    LaunchedEffect(Unit) {
//       // viewModel.loadContacts()
//    }

    val contacts = viewModel.contacts

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Group Members") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val selectedContacts = contacts.filter { it.isSelected }
                        val database = FirebaseDatabase.getInstance().reference

                        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val matchedUids = mutableListOf<String>()

                                for (contact in selectedContacts) {
                                    val contactPhone = contact.phoneNumber.filter { it.isDigit() }.takeLast(10)

                                    for (userSnapshot in snapshot.children) {
                                        val user = userSnapshot.getValue(User::class.java)
                                        val userPhone = user?.phoneNumber?.filter { it.isDigit() }?.takeLast(10)

                                        if (user != null && userPhone == contactPhone) {
                                            matchedUids.add(user.uid)
                                        }
                                    }
                                }

                                if (matchedUids.isEmpty()) {
                                    Toast.makeText(context, "No matching users found", Toast.LENGTH_SHORT).show()
                                    return
                                }

                                val updates = mutableMapOf<String, Any>()
                                for (uid in matchedUids) {
                                    updates["groups/$groupId/members/$uid"] = true
                                    updates["users/$uid/groups/$groupId"] = true
                                }

                                database.updateChildren(updates)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Members added to group", Toast.LENGTH_SHORT).show()
                                        val matchedUsers = mutableListOf<User>()
                                        for (userSnapshot in snapshot.children) {
                                            val user = userSnapshot.getValue(User::class.java)
                                            val userPhone = user?.phoneNumber?.filter { it.isDigit() }?.takeLast(10)
                                            if (user != null && selectedContacts.any { it.phoneNumber.filter { it.isDigit() }.takeLast(10) == userPhone }) {
                                                matchedUsers.add(user)
                                            }
                                        }
                                        onConfirmSelection(matchedUsers) // 👈 pass List<User> // ✅ just call the callback
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to add members", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, "Failed to read users", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }) {
                        Text("Done")
                    }

                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(contacts) { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleSelection(contact) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = contact.isSelected,
                        onCheckedChange = { viewModel.toggleSelection(contact) }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(contact.name, fontWeight = FontWeight.Bold)
                        Text(contact.phoneNumber, color = Color.Gray)
                    }
                }
            }
        }
    }
}
