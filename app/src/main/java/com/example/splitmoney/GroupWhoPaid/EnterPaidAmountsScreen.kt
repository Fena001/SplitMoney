package com.example.splitmoney.GroupWhoPaid

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.splitmoney.AddExpence.ExpenseFlowViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterPaidAmountsScreen(
    members: List<User>,
    totalExpenseAmount: Double,
    groupId: String,
    groupName: String,
    groupType: String,
    expenseId: String,
    navController: NavController,
    onBack: () -> Unit,
    onConfirm: (Map<String, Double>) -> Unit = {},
    viewModel: ExpenseFlowViewModel
) {
    val paidAmounts = remember { mutableStateMapOf<String, String>() }
    val enterPaidViewModel: EnterPaidAmountsViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        members.forEach {
            if (paidAmounts[it.uid] == null) {
                paidAmounts[it.uid] = ""
            }
        }
    }

    val totalEnteredAmount = paidAmounts.values.sumOf { it.trim().toDoubleOrNull() ?: 0.0 }
    val amountLeft = totalExpenseAmount - totalEnteredAmount

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

                                enterPaidViewModel.saveMultiplePayers(
                                    groupId = groupId,
                                    expenseId = expenseId,
                                    totalAmount = totalExpenseAmount,
                                    paymentsMap = resultMap,
                                    onSuccess = {
                                        navController.previousBackStackEntry?.savedStateHandle?.set("selectedPayerId", "multiple")
                                        navController.previousBackStackEntry?.savedStateHandle?.set("whoPaid", resultMap)

                                        val encodedName = Uri.encode(groupName)
                                        val encodedType = Uri.encode(groupType)
                                        navController.navigate("add_expense?groupId=$groupId&groupName=$encodedName&groupType=$encodedType&reset=false") {
                                            popUpTo("who_paid?groupId=$groupId&groupName=$encodedName&groupType=$encodedType") {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    onFailure = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Failed to save: ${it.message}")
                                        }
                                    }
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
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "₹%.2f of ₹%.2f".format(totalEnteredAmount, totalExpenseAmount),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "₹%.2f left".format(amountLeft),
                    color = if (amountLeft == 0.0) Color.LightGray else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
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
            items(members) { member ->
                val displayName = member.name.ifBlank { "You" }
                val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF800000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = displayName,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        TextField(
                            value = paidAmounts.getOrDefault(member.uid, ""),
                            onValueChange = { newValue ->
                                if (newValue.matches(Regex("^(\\d+)?(\\.\\d{0,2})?$"))) {
                                    paidAmounts[member.uid] = newValue
                                }
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
        }
    }
}
