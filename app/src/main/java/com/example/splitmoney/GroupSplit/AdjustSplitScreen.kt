package com.example.splitmoney.GroupSplit

import SplitEquallyTab
import User
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.splitmoney.AddExpence.ExpenseFlowViewModel
import com.example.splitmoney.Calculation.parsePercentageSplit
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustSplitScreen(
    navController: NavController,
    people: List<User>,
    groupId: String,
    groupName: String,
    groupType: String,
    totalAmount: Double,
    onDone: (Map<String, Double>) -> Unit = {},
    viewModel: ExpenseFlowViewModel,
) {
    val tabs = listOf("Equally", "Unequally", "By percentages")
    var selectedTab by remember { mutableStateOf(0) }

    val selectedMembers = remember {
        mutableStateMapOf<String, Boolean>().apply {
            people.forEach { this[it.uid] = true }
        }
    }

    val selectedCount = selectedMembers.count { it.value }
    val perPersonAmount = if (selectedCount > 0) totalAmount / selectedCount else 0.0

    val unequalAmounts = remember { mutableStateMapOf<String, String>() }
    val percentageMap = remember { mutableStateMapOf<String, String>() }

    val context = LocalContext.current

    val encodedGroupName = Uri.encode(groupName)
    val encodedGroupType = Uri.encode(groupType)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adjust split", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Log.d("SplitDebug", "Selected Tab: $selectedTab")
                        Log.d("SplitDebug", "Unequal Amounts Map: $unequalAmounts")
                        Log.d("SplitDebug", "Total Amount: $totalAmount")

                        if (groupId.isBlank() || groupName.isBlank() || groupType.isBlank()) {
                            Toast.makeText(context, "Group information missing.", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        val isValid = when (selectedTab) {
                            0 -> selectedCount > 0
                            1 -> isTotalValid(unequalAmounts, totalAmount)
                            2 -> isPercentageValid(percentageMap)
                            else -> false
                        }

                        if (!isValid) {
                            Toast.makeText(
                                context,
                                when (selectedTab) {
                                    0 -> "Select at least one person"
                                    1 -> "Total entered doesn't match ₹%.2f".format(totalAmount)
                                    2 -> "Total percentage must be 100%"
                                    else -> "Invalid input"
                                },
                                Toast.LENGTH_SHORT
                            ).show()
                            return@IconButton
                        }

                        val finalSplitMap: Map<String, Double> = when (selectedTab) {
                            0 -> selectedMembers.filterValues { it }.mapValues { perPersonAmount }
                            1 -> unequalAmounts.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
                            2 -> percentageMap.mapValues {
                                val percent = it.value.toDoubleOrNull() ?: 0.0
                                (percent / 100.0) * totalAmount
                            }
                            else -> emptyMap()
                        }

                        viewModel.setSplitMap(finalSplitMap)
                        viewModel.setSplitBetweenMap(finalSplitMap)

                        navController.navigate("expense_summary/$groupId/$encodedGroupName/$encodedGroupType")
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Done")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color.White
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            if (index == 1) unequalAmounts.clear()
                        },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> SplitEquallyTab(
                    members = people,
                    selectedMembers = selectedMembers,
                    perPersonAmount = perPersonAmount,
                    onToggleAll = {
                        val allSelected = selectedMembers.values.all { it }
                        people.forEach { selectedMembers[it.uid] = !allSelected }
                    },
                    onDone = {},
                    onSplitChanged = {},
                    viewModel = viewModel,
                )

                1 -> SplitUnequallyTab(
                    members = people,
                    totalAmount = totalAmount,
                    onBack = {},
                    onDone = {},
                    onSplitChanged = { updatedSplit ->
                        unequalAmounts.clear()
                        updatedSplit.forEach { (uid, amount) ->
                            unequalAmounts[uid] = "%.2f".format(amount)
                        }
                    },
                    viewModel = viewModel,
                )

                2 -> SplitByPercentageTab(
                    members = people,
                    totalAmount = totalAmount,
                    onBack = {},
                    onDone = {},
                    onSplitChanged = { updatedSplit ->
                        updatedSplit.forEach { (uid, amount) ->
                            percentageMap[uid] = "%.2f".format((amount / totalAmount) * 100)
                        }
                    },
                    viewModel = viewModel,
                    percentageMap = percentageMap
                )
            }
        }
    }
}

fun isApproximatelyEqual(a: Double, b: Double, epsilon: Double = 0.1): Boolean {
    return abs(a - b) < epsilon
}

fun isTotalValid(map: Map<String, String>, total: Double): Boolean {
    val sum = map.values.mapNotNull {
        val raw = it.trim()
        Log.d("SplitDebug", "Parsing value: '$raw'")
        raw.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_EVEN)
    }.fold(BigDecimal.ZERO) { acc, bd -> acc + bd }

    val roundedTotal = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_EVEN)
    Log.d("SplitDebug", "Final computed sum: $sum, Total: $roundedTotal")

    return sum.compareTo(roundedTotal) == 0
}

fun isPercentageValid(map: Map<String, String>): Boolean {
    val sum = map.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    Log.d("SplitDebug", "Percentage sum: $sum")
    return isApproximatelyEqual(sum, 100.0)
}