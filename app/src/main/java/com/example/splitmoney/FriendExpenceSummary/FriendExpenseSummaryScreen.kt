package com.example.splitmoney.friendSummary

import User
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitmoney.FriendAddExpenceScreen.FriendExpenseViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendExpenseSummaryScreen(
    friendName: String,
    friendUid: String,
    viewModel: FriendExpenseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    val splitType = viewModel.splitType
    val description = viewModel.description
    val totalAmount = viewModel.totalAmount
    val whoPaidMap = viewModel.whoPaidMap
    val splitMap = viewModel.splitMap
    val participants = viewModel.participants

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.saveExpenseToFirebase(
                        friendUid = friendUid,
                        onSuccess = {
                            Toast.makeText(context, "Expense saved!", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)) // Maroon
            ) {
                Text("Save & Continue", color = Color.White)
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Split Type: $splitType",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Description: $description", color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Paid by:", color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))

            whoPaidMap.forEach { (uid, amount) ->
                val name = if (uid == currentUserUid) "You"
                else participants.find { it.uid == uid }?.name ?: "Unknown"

                Text("• $name paid ₹%.2f".format(amount), color = Color.White, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Amount: ₹%.2f".format(totalAmount), color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(20.dp))

            Divider(color = Color.Gray)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Split Summary", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(splitMap.entries.toList()) { entry ->
                    val uid = entry.key
                    val amount = entry.value
                    val name = if (uid == currentUserUid) "You"
                    else participants.find { it.uid == uid }?.name ?: "Unknown"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, color = Color.White, fontSize = 15.sp)
                        Text("₹%.2f".format(amount), color = Color.White, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
