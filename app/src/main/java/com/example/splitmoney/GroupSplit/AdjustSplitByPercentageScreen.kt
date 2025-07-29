package com.example.splitmoney.GroupSplit

import User
import android.R.attr.singleLine
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitmoney.AddExpence.ExpenseFlowViewModel
import com.example.splitmoney.Calculation.calculateTotalPercentage
import com.example.splitmoney.Calculation.parsePercentageSplit
import kotlin.collections.set

// For your User model


@Composable
fun SplitByPercentageTab(
    viewModel: ExpenseFlowViewModel,
    members: List<User>,
    totalAmount: Double,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onSplitChanged: (Map<String, Double>) -> Unit
)
{
    LaunchedEffect(Unit) {
        viewModel.updateSplitType("by percentages")
    }
    val percentageMap = remember { mutableStateMapOf<String, String>() }

    val totalPercent = calculateTotalPercentage(percentageMap)
    val isValid = isPercentageValid(percentageMap)

    LaunchedEffect(percentageMap) {
        onSplitChanged(parsePercentageSplit(percentageMap, totalAmount))
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                "Split by percentages",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                "Enter the percentage split that's fair for your situation",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(members) { member ->
                val percentage = percentageMap[member.uid] ?: ""

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB71C1C)),
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = member.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "₹${(percentage.toFloatOrNull() ?: 0f) * totalAmount / 100}",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }

                    // Line input
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(
                            "%",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextField(
                            value = percentage,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && (newValue.toFloatOrNull() ?: 0f) <= 100) {
                                    percentageMap[member.uid] = newValue
                                }
                            },
                            placeholder = { Text("0", color = Color.LightGray) },
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
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.width(70.dp)
                        )

                    }
                }
            }
        }

        // Bottom Summary
        val totalEnteredPercentage = percentageMap.values.sumOf { it.toDoubleOrNull() ?: 0.0 }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${totalEnteredPercentage.toInt()}% of 100%",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(100 - totalEnteredPercentage).toInt()}% left",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}
