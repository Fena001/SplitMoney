package com.example.splitmoney.friendAdjustSplit

import User
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitmoney.FriendAddExpenceScreen.FriendExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendAdjustSplitScreen(
    participants: List<User>,
    totalAmount: Float,
    paidByUser: User,
    viewModel: FriendExpenseViewModel,
    onBack: () -> Unit,
    onConfirmNavigate: () -> Unit
) {
    val context = LocalContext.current
    val tabs = listOf("Equally", "Unequally", "By percentages")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val selectedTab = tabs[selectedTabIndex]

    // Local state maps
    val equallySelected = remember { mutableStateMapOf<String, Boolean>() }
    val unequalAmounts = remember { mutableStateMapOf<String, String>() }
    val percentages = remember { mutableStateMapOf<String, String>() }

    // Restore ViewModel data if present
    LaunchedEffect(Unit) {
        val restoredType = viewModel.splitType
        val restoredMap = viewModel.splitMap
        if (restoredMap.isNotEmpty()) {
            selectedTabIndex = tabs.indexOf(restoredType).coerceAtLeast(0)
            when (restoredType) {
                "Equally" -> restoredMap.keys.forEach { equallySelected[it] = true }
                "Unequally" -> restoredMap.forEach { (uid, amount) -> unequalAmounts[uid] = amount.toString() }
                "By percentages" -> restoredMap.forEach { (uid, amount) ->
                    val percent = ((amount / totalAmount) * 100).toInt()
                    percentages[uid] = percent.toString()
                }
            }
        }
    }

    // Initialize maps for all users
    LaunchedEffect(participants) {
        participants.forEach {
            unequalAmounts.putIfAbsent(it.uid, "")
            percentages.putIfAbsent(it.uid, "")
        }
    }

    // Calculations
    val selectedCount = equallySelected.values.count { it }
    val equallyAmount = if (selectedCount > 0) totalAmount / selectedCount else 0f
    val unequalTotal = unequalAmounts.values
        .mapNotNull { it.toFloatOrNull() }
        .sum()

    val percentTotal = percentages.values
        .mapNotNull { it.toFloatOrNull() }
        .sum()

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
                            Toast.makeText(context, "Fix the split before confirming.", Toast.LENGTH_SHORT).show()
                        } else {
                            val finalMap: Map<String, Float> = when (selectedTab) {
                                "Equally" -> participants
                                    .filter { equallySelected[it.uid] == true }
                                    .associate { it.uid to equallyAmount }

                                "Unequally" -> unequalAmounts.mapValues {
                                    it.value.toFloatOrNull() ?: 0f
                                }

                                "By percentages" -> percentages.mapValues {
                                    val percent = it.value.toFloatOrNull() ?: 0f
                                    (percent / 100f) * totalAmount
                                }

                                else -> emptyMap()
                            }

                            // ✅ Store to shared FriendExpenseViewModel
                           // viewModel.setSplitType(selectedTab)
                            viewModel.setSplitMap(finalMap)
                            viewModel.setSplitType(selectedTab)
                            Log.d("AdjustSplit", "Setting PaidByUser: ${paidByUser.name}")
                            viewModel.setPaidByUser(paidByUser)
                            viewModel.setTotalAmount(totalAmount)
                            viewModel.updateAmount(totalAmount.toString())

                            // ✅ DEBUG LOGS
                            Log.d("SaveClick", "Description: ${viewModel.description}")
                            Log.d("SaveClick", "Amount: ${viewModel.amount}")
                            Log.d("SaveClick", "SplitType: ${viewModel.splitType}")
                            Log.d("SaveClick", "PaidByUser: ${viewModel.paidByUser.name}")
                            Log.d("SaveClick", "SplitMap: ${viewModel.splitMap}")
                            Log.d("SaveClick", "Participants: ${viewModel.participants.map { it.name }}")

                            onConfirmNavigate()
                        }
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF4CAF50)
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

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(participants) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(getAvatarColor(user.name)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.name.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Text(user.name, modifier = Modifier.weight(1f), color = Color.White, fontSize = 16.sp)

                        when (selectedTab) {
                            "Equally" -> Checkbox(
                                checked = equallySelected[user.uid] == true,
                                onCheckedChange = { equallySelected[user.uid] = it }
                            )

                            "Unequally" -> TextField(
                                value = unequalAmounts[user.uid] ?: "",
                                onValueChange = { unequalAmounts[user.uid] = it },
                                modifier = Modifier.width(80.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color(0xFF4CAF50),
                                    unfocusedIndicatorColor = Color(0xFF4CAF50)
                                )
                            )

                            "By percentages" -> Row(verticalAlignment = Alignment.CenterVertically) {
                                TextField(
                                    value = percentages[user.uid] ?: "",
                                    onValueChange = { percentages[user.uid] = it },
                                    modifier = Modifier.width(80.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedIndicatorColor = Color(0xFF4CAF50),
                                        unfocusedIndicatorColor = Color(0xFF4CAF50)
                                    )
                                )
                                Text("%", color = Color.White, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            }

            // Footer summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                when (selectedTab) {
                    "Equally" -> {
                        Text("₹%.2f/person (%d)".format(equallyAmount, selectedCount), color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("All", color = Color.White)
                            Checkbox(
                                checked = selectedCount == participants.size,
                                onCheckedChange = { check ->
                                    participants.forEach { equallySelected[it.uid] = check }
                                }
                            )
                        }
                    }

                    "Unequally" -> Column {
                        Text("₹%.2f of ₹%.2f".format(unequalTotal, totalAmount), color = Color.White)
                        Text(
                            "₹%.2f left".format(totalAmount - unequalTotal),
                            color = if ((totalAmount - unequalTotal) == 0f) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    "By percentages" -> Column {
                        Text("%.0f%% of 100%%".format(percentTotal), color = Color.White)
                        Text(
                            "%.0f%% left".format(100f - percentTotal),
                            color = if (percentTotal == 100f) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 12.sp
                        )
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
    return colors[(name.hashCode() and 0x7fffffff) % colors.size]
}
