package com.example.splitmoney.AddGroup

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    viewModel: CreateGroupViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observing from ViewModel state
    val groupName by viewModel.groupName.collectAsState()
    val groupType by viewModel.groupType.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val groupTypes = listOf("Trip", "Home", "Couple", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Create a group", color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.createGroup(
                            onSuccess = { groupId ->
                                navController.navigate("group_detail/${groupId}/${groupName}/${groupType}")
                            },
                            onFailure = { e ->
                                Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }) {
                        Text("Done", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Group image selector
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                    .clickable {
                        // TODO: Add image picker and call viewModel.setImageUrl(url)
                    },
                contentAlignment = Alignment.Center
            ) {
                val selectedIcon = when (groupType) {
                    "Trip" -> Icons.Default.Flight
                    "Home" -> Icons.Default.Home
                    "Couple" -> Icons.Default.Favorite
                    "Other" -> Icons.AutoMirrored.Filled.ListAlt
                    else -> Icons.Default.Group
                }
                Icon(
                    selectedIcon,
                    contentDescription = groupType,
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Group name text field
            OutlinedTextField(
                value = groupName,
                onValueChange = { viewModel.setGroupName(it) },
                label = { Text("Group name", color = Color.White) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledBorderColor = Color.Gray,
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    disabledContainerColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Type", color = Color.White, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                groupTypes.forEach { type ->
                    val isSelected = type == groupType
                    OutlinedButton(
                        onClick = { viewModel.setGroupType(type) },
                        border = BorderStroke(1.dp, Color.White),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Black,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val icon = when (type) {
                                "Trip" -> Icons.Default.Flight
                                "Home" -> Icons.Default.Home
                                "Couple" -> Icons.Default.Favorite
                                "Other" -> Icons.Default.ListAlt
                                else -> Icons.Default.Group
                            }
                            Icon(icon, contentDescription = type)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(type, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

