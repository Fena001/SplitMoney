package com.example.splitmoney.FriendAddExpenceScreen

import User
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.splitmoney.IconDescription.fetchIconUrl
import com.example.splitmoney.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendAddExpenseScreen(
    friendUid: String,
    friendName: String,
    navController: NavController,
    viewModel: FriendExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val description = viewModel.description
    val amount = viewModel.amount


    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    // ✅ Restore data from previous screens
    LaunchedEffect(Unit) {
        savedStateHandle?.get<List<String>>("paidByUserIds")?.let {
            viewModel.setPaidByMultiple(it)
            savedStateHandle.remove<List<String>>("paidByUserIds")
        }

        savedStateHandle?.get<Map<String, Double>>("paidAmounts")?.let {
            viewModel.setWhoPaidMap(it)
            savedStateHandle.remove<Map<String, Double>>("paidAmounts")
        }

        savedStateHandle?.get<String>("splitType")?.let {
            viewModel.setSplitType(it)
            savedStateHandle.remove<String>("splitType")
        }

        savedStateHandle?.get<Map<String, Float>>("splitMap")?.let {
            viewModel.setSplitMap(it)
            savedStateHandle.remove<Map<String, Float>>("splitMap")
        }

        savedStateHandle?.get<List<User>>("participants")?.let {
            viewModel.setParticipants(it)
            savedStateHandle.remove<List<User>>("participants")
        }

        savedStateHandle?.get<User>("paidByUser")?.let {
            viewModel.setPaidByUser(it)
            savedStateHandle.remove<User>("paidByUser")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add expense", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (description.isBlank()) {
                            Toast.makeText(context, "Enter description", Toast.LENGTH_SHORT).show()
                        } else if (amount.toFloatOrNull() == null || amount.toFloat() <= 0f) {
                            Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Saved (not implemented)", Toast.LENGTH_SHORT).show()
                        }
                    })
                    {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose group", color = Color.White)
                }
                Row {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Cyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Magenta)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFFBA68C8))
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            // With you and friend chip
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
                Text("With you and", color = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.DarkGray,
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                friendName.firstOrNull()?.uppercase() ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(friendName, color = Color.White)
                    }
                }
            }

            // Description with icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                var iconUrl by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(description) {
                    iconUrl = fetchIconUrl(description)
                }

                val iconPainter = rememberAsyncImagePainter(
                    model = iconUrl,
                    placeholder = painterResource(id = R.drawable.img),
                    error = painterResource(id = R.drawable.img)
                )
                Image(painter = iconPainter, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                TextField(
                    value = description,
                    onValueChange = { viewModel.updateDescription(it) },
                    placeholder = { Text("Enter a description", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF4CAF50),
                        unfocusedIndicatorColor = Color(0xFF4CAF50),
                        cursorColor = Color(0xFF4CAF50),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount input
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color.LightGray)
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = amount,
                    onValueChange = { viewModel.updateAmount(it) },
                    placeholder = { Text("0.00", color = Color.White) },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF4CAF50),
                        unfocusedIndicatorColor = Color(0xFF4CAF50),
                        cursorColor = Color(0xFF4CAF50),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Payer and Split section
            FriendPayerRow(
                navController = navController,
                friendId = friendUid,
                friendName = friendName,
                paidByUserIds = viewModel.paidByUserIds,
                splitType = viewModel.splitType,
                currentUser = viewModel.currentUser,
                onPayerClick = {
                    val value = viewModel.amount.toFloatOrNull()
                    if (value == null || value <= 0f) {
                        Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    } else {
                        val encodedName = Uri.encode(friendName)
                        navController.navigate("who_paid_friend/$friendUid/$encodedName/$value")
                    }
                },
                onSplitClick = {
                    val value = viewModel.amount.toFloatOrNull()
                    if (value == null || value <= 0f) {
                        Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    } else {
                        val encodedName = Uri.encode(friendName)

                        val currentUser = viewModel.currentUser
                        val friend = User(uid = friendUid, name = friendName, email = "")
                        val participants = listOf(currentUser, friend)

                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                            set("participants", participants)
                            set("totalAmount", value)
                            set("paidByUser", currentUser)
                        }


                        viewModel.setParticipants(participants)
                        viewModel.setPaidByUser(currentUser)

                        val uids = listOf(currentUser.uid, friendUid).joinToString("-")
                        navController.navigate("adjust_split_friend/$uids")
                    }
                },
            )
        }
    }
}

@Composable
fun FriendPayerRow(
    navController: NavController,
    friendId: String,
    friendName: String,
    paidByUserIds: List<String>,
    splitType: String,
    currentUser: User,
    onPayerClick: () -> Unit,
    onSplitClick: () -> Unit
) {
    val payerName = when {
        paidByUserIds.size > 1 -> "+${paidByUserIds.size} people"
        paidByUserIds.size == 1 -> {
            val id = paidByUserIds.first()
            when (id) {
                currentUser.uid -> "You"
                friendId -> friendName
                else -> "Someone"
            }
        }
        else -> "You"
    }

    val splitLabel = when (splitType.trim().lowercase()) {
        "unequally" -> "split unequally"
        "percentages", "by percentages" -> "split by percentages"
        else -> "split equally"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Paid by", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onPayerClick() },
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
        Text("and", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onSplitClick() },
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
