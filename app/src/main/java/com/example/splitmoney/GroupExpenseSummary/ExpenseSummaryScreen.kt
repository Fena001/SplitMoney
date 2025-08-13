package com.example.splitmoney.GroupExpenseSummary

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.splitmoney.AddExpence.ExpenseFlowViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ExpenseSummaryScreen(
    navController: NavController,
    viewModel: ExpenseFlowViewModel,
    groupId: String,
    groupName: String,
    groupType: String
) {
    val context = LocalContext.current
    val title = viewModel.title
    val amount = viewModel.totalAmount.collectAsState().value
    val splitType = viewModel.splitType
    val paidBy = viewModel.paidBy.collectAsState().value
    val whoPaidMap = viewModel.whoPaidMap.collectAsState().value
    val splitBetween = viewModel.splitMap.collectAsState().value
    val members = viewModel.selectedMembers.collectAsState().value
    val expenseId = viewModel.expenseId

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            Button(
                onClick = {
                    val encodedGroupName = Uri.encode(groupName)
                    val encodedGroupType = Uri.encode(groupType)

                    navController.navigate(
                        "add_expense?groupId=$groupId&groupName=$encodedGroupName&groupType=$encodedGroupType&reset=false"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save & Continue")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Expense Summary", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(label = "Title", value = title)
            SummaryRow(label = "Amount", value = "₹%.2f".format(amount))
            SummaryRow(
                label = "Paid by",
                value = if (paidBy == "multiple")
                    whoPaidMap.entries.joinToString { entry ->
                        val name = members.find { it.uid == entry.key }?.name ?: "?"
                        "$name: ₹%.2f".format(entry.value)
                    }
                else members.find { it.uid == paidBy }?.name ?: "You"
            )
            SummaryRow(label = "Split Type", value = splitType.capitalize())

            Spacer(modifier = Modifier.height(16.dp))

            Text("Split Details", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            splitBetween.forEach { (uid, amt) ->
                val name = members.find { it.uid == uid }?.name ?: "?"
                SummaryRow(label = name, value = "₹%.2f".format(amt))
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.LightGray, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}
