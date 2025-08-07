package com.example.splitmoney.friendIndividualhome

import User
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.splitmoney.FriendAddExpenceScreen.FriendExpenseViewModel
import com.example.splitmoney.R
import com.example.splitmoney.dataclass.Expense
import com.example.splitmoney.dataclass.ExpenseItem
import androidx.compose.foundation.lazy.items



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendUid: String,
    friendName: String, // Initial passed value, used as fallback only
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: FriendDetailViewModel = remember {
        ViewModelProvider(
            context as ViewModelStoreOwner,
            FriendDetailViewModelFactory(FriendRepository(), friendUid, friendName)
        )[FriendDetailViewModel::class.java]
    }

    val uiState by viewModel.uiState.collectAsState()

    // ✅ Use resolved name from uiState
    val resolvedName = uiState.userNames[friendUid]
        ?.takeIf { it.isNotBlank() }
        ?: friendName

    Log.d("UI", "Rendering resolvedName = $resolvedName")

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("resetExpense", true)
                    navController.navigate("friend_add_expense/${friendUid}/${Uri.encode(resolvedName)}")
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add expense") },
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(padding)
        ) {
            Box {
                // Header background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFF009688))
                )

                // Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .offset(x = 24.dp, y = 115.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = resolvedName.firstOrNull()?.uppercase() ?: "",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(start = 24.dp)) {
                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = resolvedName,
                    color = Color.White,
                    fontSize = 30.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.expenses.isEmpty()) {
                    Text(
                        text = "No expenses here yet.",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "${uiState.expenses.size} expenses",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SquareButton("Settle up")
                SquareButton("Remind…")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Expense list or placeholder
            if (uiState.expenses.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No expenses here yet.", color = Color.White, fontSize = 16.sp)
                    Text(
                        "Add an expense to get this party started.",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(uiState.expenses.filterNotNull()) { expense ->
                        ExpenseCard(expense = expense, userNames = uiState.userNames)
                    }
                }
            }
        }
    }
}


@Composable
fun FriendPayerRow(
    navController: NavController,
    friendId: String,
    friendName: String,
    selectedPayerId: String,
    splitType: String,
    amount: String,
    currentUser: User,
    onPayerClick: () -> Unit,
    onSplitClick: () -> Unit
) {
    val context = LocalContext.current

    val payerName = when (selectedPayerId) {
        currentUser.uid -> "You"
        friendId -> friendName
        else -> "You"
    }

    val splitLabel = when (splitType.lowercase()) {
        "unequally" -> "unequally"
        "by percentages", "percentage" -> "by percentages"
        else -> "equally"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Paid by", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))

        // Paid By Chip
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable {
                    val value = amount.toFloatOrNull()
                    if (value == null || value <= 0f) {
                        Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    } else {
                        onPayerClick()
                    }
                },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF800000),
            contentColor = Color.White
        ) {
            Text(
                text = payerName,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(8.dp))
        Text("and split", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))

        // Split Chip
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable {
                    val value = amount.toFloatOrNull()
                    if (value == null || value <= 0f) {
                        Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    } else {
                        onSplitClick()
                    }
                },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF424242),
            contentColor = Color.White
        ) {
            Text(
                text = splitLabel,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SquareButton(text: String, icon: Int? = null) {
    OutlinedButton(
        onClick = { },
        shape = RoundedCornerShape(4.dp), // sharp corners
        border = BorderStroke(1.dp, Color.Gray),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        if (icon != null) {
            Icon(painter = painterResource(id = icon), contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text, fontSize = 14.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun FriendDetailScreenPreview() {
    // Mock navController (no actual navigation in preview)
    val navController = rememberNavController()

    FriendDetailScreen(
        friendUid = "mock_uid_123",
        friendName = "Reeva",
        navController = navController
    )
}

@Composable
fun ExpenseCard(expense: ExpenseItem, userNames: Map<String, String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(expense.description, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(expense.date, color = Color.Gray, fontSize = 14.sp)
        Text(expense.payerInfo, color = Color.LightGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Divider(color = Color.DarkGray)
    }
}