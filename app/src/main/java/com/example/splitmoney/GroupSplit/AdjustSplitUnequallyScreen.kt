package com.example.splitmoney.GroupSplit

import User
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitmoney.AddExpence.ExpenseFlowViewModel
import com.example.splitmoney.Calculation.calculateTotalEntered
import com.example.splitmoney.Calculation.isTotalValid
import com.example.splitmoney.Calculation.parseUnequalSplit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitUnequallyTab(
    viewModel: ExpenseFlowViewModel,
    members: List<User>,
    totalAmount: Double,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onSplitChanged: (Map<String, Double>) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.updateSplitType("unequally")
    }
    val amounts = remember(members) {
        mutableStateMapOf<String, String>().apply {
            members.forEach { this[it.uid] = "" }
        }
    }


    val enteredSum = amounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val remaining = totalAmount - enteredSum

    // Notify parent of updated split
    LaunchedEffect(amounts) {
        val parsed = amounts.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
        onSplitChanged(parsed)
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            val remainingColor = when {
                remaining == 0.0 -> Color(0xFFAAAAAA)
                remaining > 0.0 -> Color(0xFFFFC107) // Yellow
                else -> Color.Red
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(vertical = 16.dp, horizontal = 24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "₹%.2f of ₹%.2f".format(enteredSum, totalAmount),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (remaining >= 0) "₹%.2f left".format(remaining)
                    else "₹%.2f over".format(-remaining),
                    color = remainingColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    "Split by exact amounts",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    "Specify exactly how much each person owes.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(members) { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
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
                                text = member.name.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = member.name.ifBlank { "You" },
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text(
                                "₹",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            TextField(
                                value = amounts[member.uid] ?: "",
                                onValueChange = { newValue ->
                                    if (newValue.matches(Regex("^\\d{0,7}(\\.\\d{0,2})?$"))) {
                                        amounts[member.uid] = newValue
                                    }
                                },
                                placeholder = { Text("0.00", color = Color.LightGray) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.White,
                                    unfocusedIndicatorColor = Color.White,
                                    cursorColor = Color(0xFF4CAF50),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                modifier = Modifier
                                    .widthIn(min = 70.dp, max = 100.dp)

                            )
                        }

                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewAdjustSplitUnequallyScreen() {
    val sampleUsers = listOf(
        User(uid = "1", name = "Meha", email = "meha@example.com", phoneNumber = "1234567890"),
        User(uid = "2", name = "Aarav", email = "aarav@example.com", phoneNumber = "9876543210"),
        User(uid = "3", name = "Isha", email = "isha@example.com", phoneNumber = "5555555555")
    )

//    SplitUnequallyTab(
//        members = sampleUsers,
//        totalAmount = 900f,
//        onBack = {},
//        onDone = {}
//    )
}
