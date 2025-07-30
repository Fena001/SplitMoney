package com.example.splitmoney.GroupWhoPaid

import User
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.splitmoney.AddExpence.ExpenseFlowViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhoPaidScreen(
    navController: NavController,
    members: List<User>,
    totalAmount: Double,
    groupId: String,
    groupName: String,
    groupType: String,
    onBack: () -> Unit,
    expenseId: String,
    viewModel: ExpenseFlowViewModel
) {
    val currentUserUid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var selectedPayer by remember { mutableStateOf(currentUserUid ?: "") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Who paid?", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (selectedPayer.isNotBlank()) {
                            if (selectedPayer == "multiple") {
                                val dummyWhoPaid = members.associate {
                                    it.uid to (totalAmount / members.size).toFloat()
                                }

                                viewModel.setWhoPaid(
                                    uid = "multiple",
                                    map = dummyWhoPaid.mapValues { it.value.toDouble() }
                                )

                                navController.currentBackStackEntry?.savedStateHandle?.apply {
                                    // REMOVE expenseId from here (handled by ViewModel)
                                    set("enterPaidMembers", members)
                                    set("enterPaidAmount", totalAmount)
                                    set("groupId", groupId)
                                    set("groupName", groupName)
                                    set("groupType", groupType)
                                    set("whoPaid", dummyWhoPaid)
                                }

                                navController.navigate("enter_paid_amount/$groupId/$groupName/$groupType")
                            } else {
                                viewModel.setWhoPaid(
                                    uid = selectedPayer,
                                    map = mapOf(selectedPayer to totalAmount.toDouble())
                                )

                                navController.previousBackStackEntry?.savedStateHandle?.apply {
                                    // REMOVE: set("expenseId", ...)
                                    set("whoPaid", mapOf(selectedPayer to totalAmount.toFloat()))
                                }

                                navController.popBackStack()
                            }
                        }
                    })
                    {
                        Text("Done", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF212121))
            )
        },
        containerColor = Color(0xFF212121),
        content = { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(members) { user ->
                    PayerRow(
                        user = user,
                        selected = user.uid == selectedPayer,
                        currentUserUid = currentUserUid,
                        onClick = { selectedPayer = user.uid },

                        )
                }

                item {
                    Divider(
                        color = Color.Gray,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    PayerRowSimple(
                        name = "Multiple people",
                        selected = selectedPayer == "multiple",
                        onClick = {
                            selectedPayer = "multiple"

                            val dummyWhoPaid = members.associate {
                                it.uid to (totalAmount / members.size).toFloat()
                            }

                            // 🔁 Update ViewModel with dummy multiple payer data
                            viewModel.setWhoPaid(
                                uid = "multiple",
                                map = dummyWhoPaid.mapValues { it.value.toDouble() }
                            )

                            navController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("enterPaidMembers", members)
                                set("enterPaidAmount", totalAmount)
                                set("expenseId", expenseId)
                                set("groupId", groupId)
                                set("groupType", groupType)
                                set("selectedPayerId", "multiple")
                                set("whoPaid", dummyWhoPaid)
                            }

                            navController.navigate("enter_paid_amount/$groupId/$groupName/$groupType")
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun PayerRow(
    user: User,
    selected: Boolean,
    currentUserUid: String?,
    onClick: () -> Unit
) {

    val displayName = if (user.uid == currentUserUid) "You" else user.name
    val initial = displayName.firstOrNull()?.uppercaseChar() ?: 'Y'

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(randomColorForName(displayName)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = displayName,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PayerRowSimple(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(randomColorForName(name)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = name,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun randomColorForName(name: String): Color {
    val colors = listOf(
        Color(0xFFD32F2F), // red
        Color(0xFF1976D2), // blue
        Color(0xFF388E3C), // green
        Color(0xFFFBC02D), // yellow
        Color(0xFF7B1FA2), // purple
        Color(0xFF0288D1), // cyan
        Color(0xFF455A64), // gray-blue
    )
    return colors[abs(name.hashCode()) % colors.size]
}