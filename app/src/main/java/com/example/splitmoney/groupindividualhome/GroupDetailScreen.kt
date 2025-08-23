package com.example.splitmoney.groupindividualhome

import User
import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.splitmoney.AddExpence.ExpenseFlowViewModel
import com.example.splitmoney.IconDescription.fetchIconUrl
import com.example.splitmoney.dataclass.Expense
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import com.example.splitmoney.R


@Composable
fun GroupDetailScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupDetailViewModel,
    groupName: String,
    groupType: String,
    expenseFlowViewModel: ExpenseFlowViewModel,
    onBack: () -> Unit,
    onAddMembers: () -> Unit,
    onAddExpense: () -> Unit,
    onShareGroupLink: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsState()
    val members by viewModel.members.collectAsState()

    val groupTypeIcon = when (groupType) {
        "Trip" -> Icons.Default.Flight
        "Home" -> Icons.Default.Home
        "Couple" -> Icons.Default.Favorite
        "Other" -> Icons.Default.ListAlt
        else -> Icons.Default.Groups
    }

    LaunchedEffect(Unit) {
        viewModel.fetchExpensesForGroup(groupId)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // ✅ Store members directly into ExpenseFlowViewModel
                    expenseFlowViewModel.setSelectedMembers(members)

                    val encodedName = Uri.encode(groupName)
                    val encodedType = Uri.encode(groupType)
                    navController.navigate("add_expense?groupId=$groupId&groupName=$encodedName&groupType=$encodedType&reset=true")
                },
                text = { Text("Add expense") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                containerColor = Color(0xFF2ECC71),
                contentColor = Color.White
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFF8B0000))
                )

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .offset(x = 24.dp, y = 100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF8B0000))
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = groupTypeIcon,
                        contentDescription = "Group Type",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 172.dp)
                ) {
                    Text(
                        text = groupName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Group Members
            GroupMemberSection(
                members = members,
                onAddMembers = {
                    println("Clicked Add Members")
                    onAddMembers()
                },
                onShareGroupLink = onShareGroupLink
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                ActionButton("Settle up", Icons.Default.Money)
                ActionButton("Charts", Icons.Default.PieChart)
                ActionButton("Balances", Icons.Default.AccountBalanceWallet)
                ActionButton("Total", Icons.Default.AttachMoney)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Expense List
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            val currentUser = members.find { it.uid == currentUid }
            val usersMap = members.associateBy { it.uid }

            if (currentUser != null) {
                ExpenseListSection(
                    expenses = expenses,
                    currentUser = currentUser,
                    usersMap = usersMap
                ) { expenseId ->
                    navController.navigate("expenseDetail/$groupId/$expenseId")
                }

            }
        }
    }
}

@Composable
fun GroupMemberSection(
    members: List<User>,
    onAddMembers: () -> Unit,
    onShareGroupLink: () -> Unit
) {
    if (members.size <= 1) {
        // Only 0 or 1 member: show initial options
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            CircularButton("Add group members", Icons.Default.GroupAdd, onAddMembers)
            CircularButton("Share group link", Icons.Default.Link, onShareGroupLink)
        }
    } else {
        // Show list of member initials + "+" button
        GroupMembersRow(
            members = members,
            onAddMemberClick = onAddMembers
        )
    }
}

@Composable
fun GroupMembersRow(members: List<User>, onAddMemberClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(start = 24.dp, top = 12.dp) // Added more top padding for spacing
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show overlapping member avatars
        members.take(5).forEachIndexed { index, user ->
            val initial = user.name.firstOrNull()?.uppercase() ?: "?"
            Box(
                modifier = Modifier
                    .offset(x = (-18 * index).dp) // Increased overlap (more negative = more overlap)
                    .zIndex((members.size - index).toFloat())
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F)),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Add spacing after the last avatar so "+" doesn't overlap
        Spacer(modifier = Modifier.width((18 * members.take(5).size).dp))

        // "+" Add Member icon (no overlap)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { onAddMemberClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add member", tint = Color.Black)
        }
    }
}

@Composable
fun ActionButton(label: String, icon: ImageVector? = null) {
    Surface(
        color = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            icon?.let {
                Icon(it, contentDescription = label, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(label, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun CircularButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFD32F2F)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, textAlign = TextAlign.Center, color = Color.White)
    }
}


@SuppressLint("SimpleDateFormat")
@Composable
fun ExpenseListSection(
    expenses: List<Expense>,
    currentUser: User,
    usersMap: Map<String, User>,
    onExpenseClick: (String) -> Unit
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
                    usersMap = usersMap,
                    onClick = { onExpenseClick(expense.expenseId) }
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    currentUser: User,
    usersMap: Map<String, User>,
    onClick: () -> Unit
) {
    val userPaidAmount = expense.paidBy[currentUser.uid]?: 0.0   // keep Double
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


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // ✅ Add this
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(sdfMonth.format(date), fontSize = 12.sp, color = Color.Gray)
            Text(
                sdfDay.format(date),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
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
}

