package com.example.splitmoney.AddExpence

import User
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.splitmoney.IconDescription.fetchIconUrl
import com.example.splitmoney.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    onBack: () -> Unit,
    onSave: () -> Unit,
    groupName: String,
    groupType: String,
    groupId: String,
    viewModel: ExpenseFlowViewModel,
    shouldReset: Boolean
) {
    val context = LocalContext.current
    val selectedPayerId by viewModel.paidBy.collectAsState()
    val whoPaidMap by viewModel.whoPaidMap.collectAsState()
    val currentUser = viewModel.currentUser
    val selectedMembersFromNav = navController.previousBackStackEntry?.savedStateHandle?.get<List<User>>("selectedMembers")
    val updatedMembers = remember {
        val base = selectedMembersFromNav ?: viewModel.selectedMembers.value
        if (base.any { it.uid == currentUser.uid }) base else base + currentUser
    }

    var description by remember { mutableStateOf(viewModel.title) }
    val amount = viewModel.amount
    var hasReset by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setSelectedMembers(updatedMembers)
    }

    LaunchedEffect(shouldReset) {
        if (shouldReset && !hasReset) {
            viewModel.reset()
            description = ""
            viewModel.setSelectedMembers(updatedMembers)
            hasReset = true
        } else if (!hasReset) {
            description = viewModel.title
            if (viewModel.selectedMembers.value.isEmpty()) {
                viewModel.setSelectedMembers(updatedMembers)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF212121),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF212121),
                    titleContentColor = Color.White
                ),
                title = {
                    Text("Add expense", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val amountValue = viewModel.amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0.0) {
                            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.title = description.trim()
                            onSave()
                        }
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                }
            )
        },
        bottomBar = {
            BottomActionBar(groupType = groupType, groupName = groupName)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            WithYouAndSection(groupName = groupName, groupType = groupType)
            Spacer(Modifier.height(24.dp))
            ExpenseDescriptionField(description) {
                description = it
                viewModel.title = it
            }
            Spacer(Modifier.height(24.dp))
            ExpenseAmountField(viewModel)
            Spacer(Modifier.height(24.dp))
            PayerRowWithNavigation(
                navController = navController,
                groupId = groupId,
                groupName = groupName,
                groupType = groupType,
                selectedPayerId = selectedPayerId ?: viewModel.currentUser.uid,
                whoPaidMap = whoPaidMap.mapValues { it.value.toFloat() },
                description = description,
                amount = amount,
                selectedMembers = updatedMembers,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun WithYouAndSection(groupName: String, groupType: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("With you and:", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .border(1.dp, Color(0xFF616161), RoundedCornerShape(8.dp))
                .clickable { }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (groupType) {
                "Trip" -> Icons.Default.Flight
                "Home" -> Icons.Default.Home
                "Couple" -> Icons.Default.Favorite
                "Other" -> Icons.Default.ListAlt
                else -> Icons.Default.Groups
            }

            Icon(icon, contentDescription = "Group Icon", tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("All of $groupName", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ExpenseDescriptionField(value: String, onValueChange: (String) -> Unit) {
    var iconUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(value) {
        iconUrl = fetchIconUrl(value)
    }
    val iconPainter = rememberAsyncImagePainter(
        model = iconUrl,
        placeholder = painterResource(id = R.drawable.img),
        error = painterResource(id = R.drawable.img)
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painter = iconPainter, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
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
}

@Composable
fun ExpenseAmountField(viewModel: ExpenseFlowViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("₹", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(16.dp))
        TextField(
            value = viewModel.amount,
            onValueChange = { viewModel.amount = it },
            placeholder = { Text("0.00", color = Color.White) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
}

@Composable
fun BottomActionBar(groupType: String, groupName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF212121)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (groupType) {
            "Trip" -> Icons.Default.Flight
            "Home" -> Icons.Default.Home
            "Couple" -> Icons.Default.Favorite
            "Other" -> Icons.Default.ListAlt
            else -> Icons.Default.Groups
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = "Group Type", tint = Color(0xFFF44336))
            Text(groupName, color = Color(0xFFF44336), fontSize = 12.sp)
        }

        IconButton(onClick = {}) {
            Icon(Icons.Default.Description, contentDescription = "Note", tint = Color.White)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.Category, contentDescription = "Category", tint = Color.White)
        }
    }
}

@Composable
fun PayerRowWithNavigation(
    navController: NavController,
    groupId: String,
    groupName: String,
    groupType: String,
    selectedPayerId: String,
    whoPaidMap: Map<String, Float>,
    description: String,
    amount: String,
    selectedMembers: List<User>,
    viewModel: ExpenseFlowViewModel
) {
    val context = LocalContext.current
    val currentUser = viewModel.currentUser

    val (paidByText, splitText) = generatePayerSplitSentence(
        selectedPayerId = selectedPayerId,
        whoPaidMap = whoPaidMap,
        splitType = viewModel.splitType,
        currentUser = currentUser,
        selectedMembers = selectedMembers
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Paid by", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))

        // Paid by surface
        Surface(
            modifier = Modifier.clickable {
                val amountValue = amount.toDoubleOrNull()
                if (amountValue == null || amountValue <= 0f) {
                    Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.setSelectedMembers(selectedMembers)
                    viewModel.setTotalAmount(amountValue)
                    viewModel.updateSplitType("equally")
                    val encodedName = Uri.encode(groupName)
                    val encodedType = Uri.encode(groupType)

                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("selectedMembers", selectedMembers)
                        set("totalAmount", amount.toDoubleOrNull() ?: 0.0)
                        set("expenseId", System.currentTimeMillis().toString()) // or any ID logic you use
                    }

                    navController.navigate("who_paid?groupId=$groupId&groupName=$encodedName&groupType=$encodedType")

                }
            },
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF800000)
        ) {
            Text(
                text = paidByText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.width(8.dp))
        Text("and split", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))

        // Split method surface
        Surface(
            modifier = Modifier.clickable {
                val amountValue = amount.toFloatOrNull()
                if (amountValue == null || amountValue <= 0f) {
                    Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.setSelectedMembers(selectedMembers)
                    viewModel.setTotalAmount(amountValue.toDouble())
                    viewModel.updateSplitType(viewModel.splitType)

                    val selectedMap = selectedMembers.associate { it.uid to true }
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("members", selectedMembers)
                        set("totalAmount", amountValue.toDouble())
                        set("groupId", groupId)
                        set("expenseId", System.currentTimeMillis().toString())
                        set("expenseTitle", description)
                        set("selectedMembersMap", selectedMap)
                        set("groupType", groupType)
                        if (selectedPayerId == "multiple") {
                            set("whoPaid", whoPaidMap.mapValues { it.value.toDouble() })
                        } else {
                            set("paidById", selectedPayerId)
                        }
                    }

                    val encodedName = Uri.encode(groupName)
                    val encodedType = Uri.encode(groupType)

                    if (groupId.isNotBlank() && groupName.isNotBlank() && groupType.isNotBlank()) {
                        navController.navigate(
                            "adjust_split?groupId=$groupId&groupName=$encodedName&groupType=$encodedType"
                        )
                    } else {
                        Toast.makeText(context, "Group information is missing", Toast.LENGTH_SHORT).show()
                    }

                }
            },
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF424242)
        ) {
            Text(
                text = splitText,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

data class PayerSplitText(val payerName: String, val splitLabel: String)

fun generatePayerSplitSentence(
    selectedPayerId: String,
    whoPaidMap: Map<String, Float>,
    splitType: String,
    currentUser: User,
    selectedMembers: List<User>
): PayerSplitText {
    val payerName = when {
        selectedPayerId == "multiple" && whoPaidMap.isNotEmpty() ->
            "+ ${whoPaidMap.size} people"
        selectedPayerId == currentUser.uid -> "You"  // ✅ Use "You" instead of name
        else -> {
            val payer = selectedMembers.find { it.uid == selectedPayerId }
            payer?.name ?: "You"  // fallback to "You"
        }
    }

    val splitLabel = when (splitType.lowercase()) {
        "unequally" -> "unequally"
        "by percentages", "percentage" -> "by percentages"
        else -> "equally"
    }

    return PayerSplitText(payerName, splitLabel)
}