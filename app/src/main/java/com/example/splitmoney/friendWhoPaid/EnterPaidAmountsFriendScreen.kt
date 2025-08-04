package com.example.splitmoney.friendWhoPaid

import User
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.splitmoney.FriendAddExpenceScreen.FriendExpenseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterPaidAmountsFriendScreen(
    participants: List<User>,
    totalAmount: Double,
    currentUser: User,
    navController: NavController,
    viewModel: FriendExpenseViewModel,
    onBack: () -> Unit,
    friendUid: String,
    friendName: String
) {
    val paidAmounts = remember { mutableStateMapOf<String, String>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Initialize with existing ViewModel values if present
    LaunchedEffect(participants) {
        participants.forEach { user ->
            val previousAmount = viewModel.whoPaidMap[user.uid]?.toString()
            paidAmounts[user.uid] = previousAmount ?: ""
        }
    }

    val totalEnteredAmount = paidAmounts.values.sumOf { it.trim().toDoubleOrNull() ?: 0.0 }
    val amountLeft = totalAmount - totalEnteredAmount

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
                            if (amountLeft == 0.0) {
                                val resultMap = paidAmounts.mapValues { it.value.trim().toDoubleOrNull() ?: 0.0 }

                                // ✅ Update ViewModel correctly
                                viewModel.setPaidBy("multiple")
                                viewModel.setPaidByMultiple(resultMap.keys.toList())
                                viewModel.setWhoPaidMap(resultMap)

                                // ✅ Navigate back to FriendAddExpenseScreen
                                val encodedName = Uri.encode(friendName)
                                navController.popBackStack(
                                    route = "friend_add_expense/$friendUid/$encodedName",
                                    inclusive = false
                                )
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Amounts don't match. Please check.")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF212121))
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White,
                    snackbarData = data
                )
            }
        },
        containerColor = Color(0xFF212121)
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(participants) { user ->
                val name = user.name.ifBlank { "You" }
                val initial = name.firstOrNull()?.uppercase() ?: "?"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF800000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(name, color = Color.White, modifier = Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹", color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        TextField(
                            value = paidAmounts[user.uid] ?: "",
                            onValueChange = { newVal ->
                                if (newVal.matches(Regex("^(\\d+)?(\\.\\d{0,2})?$")))
                                    paidAmounts[user.uid] = newVal
                            },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(80.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color(0xFF4CAF50),
                                unfocusedIndicatorColor = Color(0xFF4CAF50),
                                cursorColor = Color(0xFF4CAF50),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "₹%.2f of ₹%.2f".format(totalEnteredAmount, totalAmount),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "₹%.2f left".format(amountLeft),
                        color = if (amountLeft == 0.0) Color.LightGray else Color.Red,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

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
