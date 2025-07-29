package com.example.splitmoney.friendIndividualhome

import User
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.splitmoney.R
import com.example.splitmoney.dataclass.Expense


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendUid: String,
    friendName: String,
    navController: NavController
) {
    val expenses = remember { mutableStateListOf<Expense>() } // TODO: fetch from Firebase

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate("friend_add_expense/${friendUid}/${Uri.encode(friendName)}")
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
                // ✅ Green header (150.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFF009688))
                )

                // ✅ Avatar slightly below green into black section
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .offset(x = 24.dp, y = 115.dp) // aligned to middle of overlap
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        friendName.firstOrNull()?.uppercase() ?: "",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            //Spacer(modifier = Modifier.height(60.dp)) // Adjusted after avatar

            Column(modifier = Modifier.padding(start = 24.dp)) {
                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = friendName,
                    color = Color.White,
                    fontSize = 30.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No expenses here yet.",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rectangular Action Buttons
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

            // Empty state + arrow
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