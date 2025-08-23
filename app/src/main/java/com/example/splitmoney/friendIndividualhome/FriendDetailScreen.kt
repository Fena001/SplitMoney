package com.example.splitmoney.friendIndividualhome

import User
import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.itemsIndexed
import coil.compose.rememberAsyncImagePainter
import com.example.splitmoney.IconDescription.fetchIconUrl
import com.example.splitmoney.groupindividualhome.ExpenseListSection
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date


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
            if (uiState.rawExpenses.isNotEmpty()) {
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val currentUser = User(uid = currentUid, name = uiState.userNames[currentUid] ?: "You", email = "")
                val usersMap = uiState.userNames.mapValues { (uid, name) -> User(uid = uid, name = name, email = "") }

                // debug
                Log.d("UI_DEBUG", "rawExpenses size = ${uiState.rawExpenses.size}, userNames=${uiState.userNames.keys}")

                ExpenseListSection(
                    expenses = uiState.rawExpenses,
                    currentUser = currentUser,
                    usersMap = usersMap
                )
            } else {
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
            }

////                LazyColumn(modifier = Modifier.fillMaxWidth()) {
////                    items(uiState.expenses.filterNotNull()) { expense ->
////                        ExpenseCard(expense = expense, userNames = uiState.userNames)
////                    }
////                }
//                val currentUid = FirebaseAuth.getInstance().currentUser?.uid
//                val currentUser = members.find { it.uid == currentUid }
//                val usersMap = members.associateBy { it.uid }
//
//                if (currentUser != null) {
//                    ExpenseListSection(
//                        expenses = expenses,
//                        currentUser = currentUser,
//                        usersMap = usersMap
//                    )
//                }

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


@SuppressLint("SimpleDateFormat")
@Composable
fun ExpenseListSection(
    expenses: List<Expense>,
    currentUser: User,
    usersMap: Map<String, User>
) {
    val grouped = expenses.groupBy {
        val date = Date(it.timestamp)
        SimpleDateFormat("MMMM yyyy").format(date) // e.g., July 2025
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        grouped.forEach { (month, items) ->
            item {
                Text(
                    text = month,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            itemsIndexed(items) { _, expense ->
                ExpenseItem(
                    expense = expense,
                    currentUser = currentUser,
                    usersMap = usersMap
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    currentUser: User,
    usersMap: Map<String, User>
) {
    val userPaidAmount = expense.paidBy[currentUser.uid] ?: 0.0   // keep Double
    val userOwesAmount = expense.splitBetween[currentUser.uid] ?: 0.0    // also Double
    val net = userPaidAmount - userOwesAmount

    val (status, color, amountDisplay) = when {
        net > 0 -> Triple("you lent", Color(0xFF2ECC71), "+₹%.2f".format(net))
        net < 0 -> Triple("you borrowed", Color(0xFFE74C3C), "-₹%.2f".format(-net))
        else -> Triple("settled", Color.Gray, "₹0.00")
    }

    val mainPayer = expense.paidBy.entries.firstOrNull()?.key
    val payerName = when {
        expense.paidBy.size > 1 -> "${expense.paidBy.size} people paid"
        mainPayer == currentUser.uid -> "You paid"
        mainPayer != null -> "${usersMap[mainPayer]?.name ?: "Someone"} paid"
        else -> "Someone paid"
    }

    val date = Date(expense.timestamp)
    val sdfMonth = SimpleDateFormat("MMM")
    val sdfDay = SimpleDateFormat("dd")
    var iconUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(expense.title) {
        iconUrl = fetchIconUrl(expense.title)
    }

    val iconPainter = rememberAsyncImagePainter(
        model = iconUrl,
        placeholder = painterResource(id = R.drawable.img),
        error = painterResource(id = R.drawable.img)
    )


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(sdfMonth.format(date), fontSize = 12.sp, color = Color.Gray)
            Text(sdfDay.format(date), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.width(20.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFD32F2F)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = iconPainter,
                contentDescription = "Expense icon",
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Text("$payerName ₹%.2f".format(expense.amount), fontSize = 12.sp, color = Color.Gray)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(status, fontSize = 12.sp, color = color)
            Text(amountDisplay, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        }
    }
}