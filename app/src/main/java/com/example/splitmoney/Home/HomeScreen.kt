@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package com.example.splitmoney.Home

import Friend
import Group
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val teal = Color(0xFF3DAB94)
    val green = Color(0xFF4CAF50)
    val red = Color(0xFFE53935)
    val darkGray = Color(0xFF5A5A5A)

    val tabs = listOf("FRIENDS", "GROUPS", "ACTIVITY")

    val userName by viewModel.userName
    val groups by viewModel.groups.collectAsState()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var selectedTab by remember { mutableStateOf(0) } // default FRIENDS tab

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    LaunchedEffect(userId) {
        viewModel.fetchGroups(userId)
    }



    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getLiveData<Int>("selectedTab")?.observeForever { tabIndex ->
            selectedTab = tabIndex
            savedStateHandle.remove<Int>("selectedTab") // reset after reading
        }
    }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.uid?.let { viewModel.fetchUserName(it) }
    }
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.uid?.let {
            viewModel.fetchUserName(it)
            viewModel.fetchFriends()
            viewModel.fetchGroups(it) // 👈 This ensures friends list is loaded/refreshed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SPLITWISE", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = teal)
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> { // FRIENDS
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("contact_picker")
                        },
                        containerColor = green
                    ) {
                        Text("+", color = Color.White, fontSize = 24.sp)
                    }
                }
                1 -> { // GROUPS
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("create_group") // group mode
                        },
                        containerColor = green
                    ) {
                        Text("+", color = Color.White, fontSize = 24.sp)
                    }
                }
                else -> {} // No FAB on ACTIVITY
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = teal,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, green, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = if (userName.isNotEmpty()) userName.first().uppercase() else "?"
                        Text(initial, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = green)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(userName, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Balance Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BalanceItem("You are owed", "1500", green)
                    BalanceItem("You owe", "750", red)
                    BalanceItem("Total Balance", "750", teal)
                }
            }

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, title ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedTab = index }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (selectedTab == index) teal else darkGray,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                        if (selectedTab == index) {
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(40.dp)
                                    .background(teal)
                            )
                        }
                    }
                }
            }
            val friends by viewModel.friends.collectAsState()

            // Dynamic List Content
            when (selectedTab) {
                0 -> FriendList(friends, navController)
                1 -> GroupList(groups, navController)
//                2 -> ActivityList()
            }
        }
    }
}

@Composable
fun BalanceItem(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("₹$amount", fontWeight = FontWeight.Bold, color = color, fontSize = 20.sp)
    }
}
@Composable
fun FriendList(friends: List<Friend>, navController: NavController) {
    val owesColor = Color(0xFFE67C4A) // Orange like screenshot
    val owedColor = Color(0xFF4CAF50) // Green for "owes you"

    LazyColumn {
        items(friends) { friend ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val encodedName = Uri.encode(friend.name)
                        navController.navigate("friend_detail/${friend.uid}/$encodedName")
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle with Initial
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        friend.name.firstOrNull()?.uppercase() ?: "",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Friend name
                Text(
                    text = friend.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                // Balance display
                val balanceText: String
                val balanceColor: Color
                val absAmount = "₹${"%.2f".format(kotlin.math.abs(friend.balance))}"

                if (friend.balance < 0) {
                    balanceText = "you owe"
                    balanceColor = owesColor
                } else if (friend.balance > 0) {
                    balanceText = "owes you"
                    balanceColor = owedColor
                } else {
                    balanceText = "settled up"
                    balanceColor = Color.Gray
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(balanceText, color = balanceColor, fontSize = 12.sp)
                    Text(absAmount, color = balanceColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Divider()
        }
    }
}


@Composable
fun GroupList(groups: List<Group>, navController: NavController) {
    val owesColor = Color(0xFFE67C4A)
    val owedColor = Color(0xFF4CAF50)

    LazyColumn {
        items(groups) { group ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val encodedName = Uri.encode(group.name ?: "")
                        val encodedType = Uri.encode(group.type ?: "")
                        navController.navigate(
                            "group_detail/${group.groupId}/$encodedName/$encodedType"
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon/Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        group.name.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)

                    Text(
                        when {
                            group.netBalance > 0 -> "${group.otherParty} owes you"
                            group.netBalance < 0 -> "You owe ${group.otherParty}"
                            else -> "You are all settled up"
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                val absAmount = "₹${kotlin.math.abs(group.netBalance)}"
                val balanceColor = when {
                    group.netBalance > 0 -> owedColor
                    group.netBalance < 0 -> owesColor
                    else -> Color.Gray
                }

                Text(
                    absAmount,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = balanceColor
                )
            }
            Divider()
        }
    }
}