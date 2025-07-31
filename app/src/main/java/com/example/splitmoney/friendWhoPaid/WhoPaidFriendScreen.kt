package com.example.splitmoney.friendWhoPaid

import User // Replace with your actual User model import
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhoPaidFriendScreen(
    navController: NavController,
    currentUser: User,
    friend: User,
    selectedPayerId: String = currentUser.uid,
    onBack: () -> Unit,
    onDone: (String) -> Unit,
    onMultiplePeopleClick: () -> Unit
) {
    var selectedId by remember { mutableStateOf(selectedPayerId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Who paid?", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set("paidByUserIds", listOf(selectedId))
                            set("splitType", "equally") // default splitType, can be updated later
                        }
                        onDone(selectedId)
                    }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    PayerRow(
                        user = currentUser,
                        isSelected = selectedId == currentUser.uid,
                        onClick = { selectedId = currentUser.uid }
                    )
                }
                item {
                    PayerRow(
                        user = friend,
                        isSelected = selectedId == friend.uid,
                        onClick = { selectedId = friend.uid }
                    )
                }
                item {
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMultiplePeopleClick() }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Multiple people",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PayerRow(
    user: User,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
                text = user.name.firstOrNull()?.uppercase() ?: "",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = user.name,
            color = Color.White,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color(0xFF4CAF50) // green tick
            )
        }
    }
}

fun getAvatarColor(name: String): Color {
    return when ((name.hashCode() and 0xFFFFFF) % 5) {
        0 -> Color(0xFFE57373)
        1 -> Color(0xFFBA68C8)
        2 -> Color(0xFF64B5F6)
        3 -> Color(0xFFFFD54F)
        else -> Color.Gray
    }
}
