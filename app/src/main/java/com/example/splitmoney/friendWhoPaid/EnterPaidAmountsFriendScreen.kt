package com.example.splitmoney.friendWhoPaid

import User
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterPaidAmountsFriendScreen(
    participants: List<User>,
    totalAmount: Double,
    currentUser: User, // Pass current user here
    onBack: () -> Unit,
    onConfirm: (Map<String, Double>) -> Unit
) {
    val context = LocalContext.current
    val amounts = remember { mutableStateMapOf<String, String>() }

    // Initialize all amounts to "0.00"
    LaunchedEffect(participants) {
        participants.forEach { user ->
            amounts.putIfAbsent(user.uid, "0.00")
        }
    }

    val totalEntered = amounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val amountLeft = totalAmount - totalEntered
    val isComplete = amountLeft == 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter paid amounts", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isComplete) {
                                onConfirm(amounts.mapValues { it.value.toDoubleOrNull() ?: 0.0 })
                            } else {
                                Toast.makeText(context, "Please distribute full amount", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(participants) { user ->
                    val isCurrentUser = user.uid == currentUser.uid
                    val displayName = if (isCurrentUser) "You" else user.name
                    val initial = displayName.firstOrNull()?.uppercase() ?: "?"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(getFriendAvatarColor(user.name)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Name
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // ₹ Symbol
                        Text(
                            text = "₹",
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Amount TextField
                        TextField(
                            value = amounts[user.uid] ?: "0.00",
                            onValueChange = {
                                amounts[user.uid] = it
                            },
                            singleLine = true,
                            //keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(80.dp),
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.White,
                                unfocusedIndicatorColor = Color.White,
                                cursorColor = Color.White
                            )
                        )
                    }
                }
            }

            // Bottom Summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "₹%.2f of ₹%.2f".format(totalEntered, totalAmount),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "₹%.2f left".format(amountLeft),
                    color = if (amountLeft == 0.0) Color(0xFF4CAF50) else Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// You can use this helper to generate a background color for each user avatar
fun getFriendAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8),
        Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFF64B5F6),
        Color(0xFF4DD0E1), Color(0xFF4DB6AC), Color(0xFF81C784),
        Color(0xFFFFB74D), Color(0xFFA1887F), Color(0xFF90A4AE)
    )
    val index = (name.hashCode() and 0x7fffffff) % colors.size
    return colors[index]
}
