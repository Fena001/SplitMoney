package com.example.splitmoney.friendAdjustSplit

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendAdjustSplitScreen(
    participants: List<User>,
    totalAmount: Float,
    paidByUser: User,
    onBack: () -> Unit,
    onConfirm: (Map<String, Float>) -> Unit
) {
    val tabs = listOf("Equally", "Unequally", "By percentages")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val selectedTab = tabs[selectedTabIndex]

    val context = LocalContext.current

    // States for each mode
    val equallySelected = remember { mutableStateMapOf<String, Boolean>() }
    val unequalAmounts = remember { mutableStateMapOf<String, String>() }
    val percentages = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(participants) {
        participants.forEach {
            equallySelected[it.uid] = true
            unequalAmounts[it.uid] = ""       // instead of "0.00"
            percentages[it.uid] = ""          // instead of "0"
        }
    }
    val selectedCount = equallySelected.values.count { it }
    val equallyAmount = if (selectedCount > 0) totalAmount / selectedCount else 0f
    val unequalTotal = unequalAmounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }.toFloat()
    val percentTotal = percentages.values.sumOf { it.toDoubleOrNull() ?: 0.0 }.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adjust split", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val isValid = when (selectedTab) {
                            "Equally" -> selectedCount > 0
                            "Unequally" -> unequalTotal == totalAmount
                            "By percentages" -> percentTotal == 100f
                            else -> false
                        }
                        if (!isValid) {
                            Toast.makeText(context, "Fix the split first", Toast.LENGTH_SHORT).show()
                        } else {
                            val result = when (selectedTab) {
                                "Equally" -> participants.filter { equallySelected[it.uid] == true }.associate { it.uid to equallyAmount }
                                "Unequally" -> unequalAmounts.mapValues { it.value.toFloatOrNull() ?: 0f }
                                "By percentages" -> percentages.mapValues {
                                    ((it.value.toFloatOrNull() ?: 0f) / 100f) * totalAmount
                                }
                                else -> emptyMap()
                            }
                            onConfirm(result)
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF4CAF50),
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTabIndex == index) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    )
                }
            }

            // Tab Illustration + Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Text(
                    when (selectedTab) {
                        "Equally" -> "Split equally"
                        "Unequally" -> "Split by exact amounts"
                        "By percentages" -> "Split by percentages"
                        else -> ""
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when (selectedTab) {
                        "Equally" -> "Select which people owe an equal share."
                        "Unequally" -> "Specify exactly how much each person owes."
                        "By percentages" -> "Enter the percentage split that's fair for your situation."
                        else -> ""
                    },
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // Participant List
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(participants) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(getAvatarColor(user.name)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.first().uppercaseChar().toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        // Name
                        Text(
                            user.name,
                            modifier = Modifier.weight(1f),
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        when (selectedTab) {
                            "Equally" -> Checkbox(
                                checked = equallySelected[user.uid] == true,
                                onCheckedChange = { equallySelected[user.uid] = it }
                            )

                            "Unequally" -> TextField(
                                value = unequalAmounts[user.uid] ?: "",
                                onValueChange = { unequalAmounts[user.uid] = it },
                                placeholder = { Text("0.00", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.width(80.dp),
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.White,
                                    unfocusedIndicatorColor = Color.Gray,
                                    cursorColor = Color.White
                                )
                            )

                            "By percentages" -> Row {
                                TextField(
                                    value = percentages[user.uid] ?: "",
                                    onValueChange = { percentages[user.uid] = it },
                                    placeholder = { Text("0", color = Color.Gray) },
                                    singleLine = true,
                                    modifier = Modifier.width(60.dp),
                                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.White,
                                        unfocusedIndicatorColor = Color.Gray,
                                        cursorColor = Color.White
                                    )
                                )
                                Text("%", color = Color.White, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            }

            // Bottom Summary
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (selectedTab) {
                    "Equally" -> {
                        Text(
                            "₹%.2f/person (%d people)".format(equallyAmount, selectedCount),
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("All", color = Color.White)
                            Checkbox(
                                checked = selectedCount == participants.size,
                                onCheckedChange = { check ->
                                    participants.forEach { equallySelected[it.uid] = check }
                                }
                            )
                        }
                    }

                    "Unequally" -> {
                        Column {
                            Text(
                                "₹%.2f of ₹%.2f".format(unequalTotal, totalAmount),
                                color = Color.White
                            )
                            Text(
                                "₹%.2f left".format(totalAmount - unequalTotal),
                                color = if (totalAmount - unequalTotal == 0f) Color(0xFF4CAF50) else Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }

                    "By percentages" -> {
                        Column {
                            Text(
                                "%.0f%% of 100%%".format(percentTotal),
                                color = Color.White
                            )
                            Text(
                                "%.0f%% left".format(100 - percentTotal),
                                color = if (percentTotal == 100f) Color(0xFF4CAF50) else Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8),
        Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFF64B5F6),
        Color(0xFF4DD0E1), Color(0xFF4DB6AC), Color(0xFF81C784),
        Color(0xFFFFB74D), Color(0xFFA1887F), Color(0xFF90A4AE)
    )
    val index = (name.hashCode() and 0x7fffffff) % colors.size
    return colors[index]
}