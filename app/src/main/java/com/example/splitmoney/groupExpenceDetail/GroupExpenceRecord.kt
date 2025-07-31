//package com.example.splitmoney.groupExpenceDetail
//
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AccountBox
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import coil.compose.rememberAsyncImagePainter
//import com.example.splitmoney.IconDescription.fetchIconUrl
//import com.example.splitmoney.R
//import com.example.splitmoney.groupindividualhome.GroupDetailViewModel
//import com.example.splitmoney.groupindividualhome.GroupDetailViewModelFactory
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun GroupExpenseRecord(
//    viewModel: GroupDetailViewModel,
//    onAddExpenseClick: () -> Unit,
//    onExpenseClick: (String) -> Unit,
//    onSettleUpClick: () -> Unit,
//    onChartsClick: () -> Unit,
//    onBalancesClick: () -> Unit
//) {
//    val state = viewModel.uiState.collectAsState().value
//
//    val viewModel: GroupDetailViewModel = viewModel(
//        factory = GroupDetailViewModelFactory(repository, savedStateHandle)
//    )
//
//    Scaffold(
//        containerColor = Color.Black,
//        floatingActionButton = {
//            FloatingActionButton(onClick = onAddExpenseClick, containerColor = Color(0xFF4CAF50)) {
//                Text("Add expense", color = Color.White)
//            }
//        },
//        bottomBar = {
//            BottomNavigationBar(currentScreen = "Groups")
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .background(Color.Black)
//        ) {
//            // Top Bar + Group Info
//            TopAppBar(title = { Text("", color = Color.White) }, navigationIcon = {})
//            GroupHeader(state)
//
//            // Quick Actions
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                ActionButton("Settle up", Color(0xFFFF9800), onSettleUpClick)
//                ActionButton("Charts", Color(0xFF6A1B9A), onChartsClick)
//                ActionButton("Balances", Color.DarkGray, onBalancesClick)
//            }
//
//            // Expense List
//            LazyColumn(modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)) {
//                item {
//                    Text("July 2025", color = Color.LightGray, fontSize = 16.sp)
//                }
//                items(state.expenses) { expense ->
//                    ExpenseItem(expense, onExpenseClick)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun GroupHeader(state: GroupDetailUiState) {
//    var iconUrl by remember { mutableStateOf<String?>(null) }
//    LaunchedEffect(state.groupName) {
//        iconUrl = fetchIconUrl(state.groupName)
//    }
//
//    val iconPainter = rememberAsyncImagePainter(
//        model = iconUrl,
//        placeholder = painterResource(id = R.drawable.img),
//        error = painterResource(id = R.drawable.img)
//    )
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Image(
//            painter = iconPainter,
//            contentDescription = null,
//            modifier = Modifier.size(48.dp)
//        )
//        Text(state.groupName, fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
//        Spacer(Modifier.height(4.dp))
//        Text(
//            text = if (state.overallBalance >= 0) "You are owed ₹%.2f".format(state.overallBalance)
//            else "You owe ₹%.2f".format(-state.overallBalance),
//            color = if (state.overallBalance >= 0) Color(0xFF4CAF50) else Color(0xFFE74C3C)
//        )
//        Spacer(Modifier.height(8.dp))
//        state.individualBalances.forEach {
//            Text(
//                text = if (it.amount >= 0) "${it.userName} owes you ₹%.2f".format(it.amount)
//                else "You owe ${it.userName} ₹%.2f".format(-it.amount),
//                color = if (it.amount >= 0) Color(0xFF4CAF50) else Color(0xFFE74C3C),
//                fontSize = 14.sp
//            )
//        }
//    }
//}
//
//@Composable
//fun ActionButton(text: String, backgroundColor: Color, onClick: () -> Unit) {
//    Button(
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
//    ) {
//        Text(text = text, color = Color.White)
//    }
//}
//
//@Composable
//fun ExpenseItem(item: ExpenseItem, onClick: (String) -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//            .clickable { onClick(item.expenseId) }
//    ) {
//        Text(text = item.date, color = Color.LightGray, fontSize = 12.sp)
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column {
//                Text(text = item.description, color = Color.White, fontSize = 16.sp)
//                Text(text = item.payerInfo, color = Color.Gray, fontSize = 14.sp)
//            }
//            Text(
//                text = if (item.userBalanceForExpense >= 0) "You lent ₹%.2f".format(item.userBalanceForExpense)
//                else "You borrowed ₹%.2f".format(-item.userBalanceForExpense),
//                color = if (item.userBalanceForExpense >= 0) Color(0xFF4CAF50) else Color(0xFFE74C3C),
//                fontSize = 14.sp
//            )
//        }
//    }
//}
//
//@Composable
//fun BottomNavigationBar(currentScreen: String) {
//    NavigationBar(containerColor = Color.Black) {
//        val items = listOf("Groups", "Friends", "Activity", "Account")
//        items.forEach {
//            NavigationBarItem(
//                selected = it == currentScreen,
//                onClick = { /* TODO */ },
//                icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
//                label = { Text(it) },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = Color(0xFF4CAF50),
//                    unselectedIconColor = Color.LightGray
//                )
//            )
//        }
//    }
//}
