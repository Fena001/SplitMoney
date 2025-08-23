package com.example.splitmoney.GroupExpenceimport

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.splitmoney.GroupExpence.ExpenseDetailViewModel
import com.example.splitmoney.IconDescription.fetchIconUrl
import com.example.splitmoney.R
import com.example.splitmoney.dataclass.Expense


@Composable
fun ExpenseDetailScreen(
    groupId: String,
    expenseId: String,
    onBack: () -> Unit,
    viewModel: ExpenseDetailViewModel = viewModel()
) {
    val expense = viewModel.expense.observeAsState().value
    val userNameMap = viewModel.userNameMap.observeAsState(emptyMap()).value

    LaunchedEffect(Unit) {
        viewModel.fetchExpense(groupId, expenseId)
    }


    LaunchedEffect(Unit) {
        viewModel.fetchExpense(groupId, expenseId)
    }
    var iconUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(expense?.title) {
        iconUrl = fetchIconUrl(expense?.title ?: "")
    }

    val iconPainter = rememberAsyncImagePainter(
        model = iconUrl,
        placeholder = painterResource(id = R.drawable.img),
        error = painterResource(id = R.drawable.img)
    )
    if (expense == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        ExpenseDetailContent(expense, userNameMap, onBack, iconPainter)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailContent(expense: Expense,
                         userNameMap: Map<String, String>,
                         onBack: () -> Unit,
                         iconPainter: Painter) {
    Scaffold(
        topBar = {
            // Custom top strip instead of default TopAppBar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp)
                    .background(Color(0xFFDCE8D6)) // Light greenish like your screenshot
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { /* Take receipt */ }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Add Receipt")
                    }
                    IconButton(onClick = { /* Edit */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { /* Delete */ }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }

                // Big icon and text centered
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = iconPainter,
                        contentDescription = "Expense icon",
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = expense.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${"%.2f".format(expense.amount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Paid by section
            // Split mapping visualization

            Spacer(Modifier.height(8.dp))
            SplitMapping(expense, userNameMap)


            Spacer(Modifier.height(20.dp))

            // Comments placeholder
            Text("Comments", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { /* update state */ },
                placeholder = { Text("Add a comment") },
                trailingIcon = {
                    IconButton(onClick = { /* send */ }) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SplitMapping(expense: Expense, userNameMap: Map<String, String>) {
    val payers = expense.paidBy.entries.toList()

    Column {
        if (payers.size == 1) {
            // ✅ Case 1: Only one payer
            val (payerUid, paidAmount) = payers.first()
            val payerName = userNameMap[payerUid] ?: payerUid

            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCircle(payerName)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$payerName paid ₹${"%.2f".format(paidAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            }

            // List who owes
            Column(modifier = Modifier.padding(start = 20.dp)) {
                expense.splitBetween.forEach { (personUid, oweAmount) ->
                    if (oweAmount > 0f) {
                        val personName = userNameMap[personUid] ?: personUid
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(Color.Gray)
                            )
                            Spacer(Modifier.width(8.dp))
                            AvatarCircle(personName)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$personName owes ₹${"%.2f".format(oweAmount)}",
                                fontSize = 19.sp
                            )
                        }
                    }
                }
            }
        } else {
            // ✅ Case 2: Multiple payers
            Text(
                text = "Multiple people paid:",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )

            payers.forEach { (payerUid, paidAmount) ->
                val payerName = userNameMap[payerUid] ?: payerUid
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarCircle(payerName)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$payerName paid ₹${"%.2f".format(paidAmount)}",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Split details:",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )

            // List who owes
            expense.splitBetween.forEach { (personUid, oweAmount) ->
                if (oweAmount > 0f) {
                    val personName = userNameMap[personUid] ?: personUid
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        AvatarCircle(personName)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "$personName owes ₹${"%.2f".format(oweAmount)}",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AvatarCircle(name: String) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(Color(0xFF3F51B5), shape = androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}



//@Preview(showBackground = true)
//@Composable
//fun ExpenseDetailContentPreview() {
//    val mockExpense = Expense(
//
//        title = "Dinner at Pizza Place",
//        amount = 1200f, // Float value
//        paidBy = mapOf("UserA" to 1200f), // Floats here too
//        splitBetween = mapOf("UserA" to 600f, "UserB" to 600f) // Floats here
//    )
//
//    val mockPainter: Painter = painterResource(id = R.drawable.img)
//
//    ExpenseDetailContent(
//        expense = mockExpense,
//        onBack = {},
//        iconPainter = mockPainter
//    )
//}
//
