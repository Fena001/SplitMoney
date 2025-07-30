package com.example.splitmoney

import LoginScreen
import User
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.splitmoney.AddExpence.AddExpenseScreen
import com.example.splitmoney.AddExpence.ExpenseFlowViewModel
import com.example.splitmoney.AddGroup.CreateGroupScreen
import com.example.splitmoney.FriendAddExpenceScreen.FriendAddExpenseScreen
import com.example.splitmoney.FriendAddExpenceScreen.FriendExpenseViewModel
import com.example.splitmoney.GroupContacts.SelectContactsScreen
import com.example.splitmoney.GroupExpenseSummary.ExpenseSummaryScreen
import com.example.splitmoney.GroupSplit.AdjustSplitScreen
import com.example.splitmoney.Home.HomeScreen
import com.example.splitmoney.Home.HomeViewModel
import com.example.splitmoney.GroupWhoPaid.EnterPaidAmountsScreen
import com.example.splitmoney.GroupWhoPaid.WhoPaidScreen
import com.example.splitmoney.friendAdjustSplit.FriendAdjustSplitScreen
import com.example.splitmoney.friendAdjustSplit.FriendAdjustSplitViewModel
import com.example.splitmoney.friendAdjustSplit.FriendAdjustSplitViewModelFactory
import com.example.splitmoney.friendContact.ContactPickerScreen
import com.example.splitmoney.friendIndividualhome.FriendDetailScreen
import com.example.splitmoney.friendWhoPaid.EnterPaidAmountsFriendScreen
import com.example.splitmoney.friendWhoPaid.WhoPaidFriendScreen
import com.example.splitmoney.groupindividualhome.GroupDetailScreen
import com.example.splitmoney.groupindividualhome.GroupDetailViewModel
import com.example.splitmoney.groupindividualhome.GroupDetailViewModelFactory
import com.example.splitmoney.signupLogin.AuthChoiceScreen
import com.example.splitmoney.signupLogin.SignUpScreen
import com.example.splitmoney.signupLogin.SplashScreen
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.google.firebase.FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val expenseFlowViewModel: ExpenseFlowViewModel = viewModel()

            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") { SplashScreen(navController) }
                composable("auth_choice") { AuthChoiceScreen(navController) }
                composable("login") { LoginScreen(navController) }
                composable("signup") { SignUpScreen(navController) }

                composable("home") {
                    val homeViewModel: HomeViewModel = viewModel()
                    HomeScreen(navController, viewModel = homeViewModel)
                }

                composable("create_group") { CreateGroupScreen(navController) }

                composable(
                    "group_detail/{groupId}/{groupName}/{groupType}",
                    arguments = listOf(
                        navArgument("groupId") { type = NavType.StringType },
                        navArgument("groupName") { type = NavType.StringType },
                        navArgument("groupType") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                    val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                    val groupType = backStackEntry.arguments?.getString("groupType") ?: ""

                    val viewModel: GroupDetailViewModel = viewModel(
                        factory = GroupDetailViewModelFactory(groupId)
                    )

                    GroupDetailScreen(
                        navController = navController,
                        groupId = groupId,
                        groupName = groupName,
                        groupType = groupType,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onAddMembers = {
                            val encodedName = Uri.encode(groupName)
                            val encodedType = Uri.encode(groupType)
                            navController.navigate("select_contacts/$groupId/$encodedName/$encodedType")
                        },
                        onShareGroupLink = {},
                        onAddExpense = {
                            val members = viewModel.members.value

                            // Always reset before navigating
                            expenseFlowViewModel.clear() // Optional, if you want to fully reset previous state
                            expenseFlowViewModel.setSelectedMembers(members)

                            val encodedName = Uri.encode(groupName)
                            val encodedType = Uri.encode(groupType)
                            navController.navigate("add_expense?groupId=$groupId&groupName=$encodedName&groupType=$encodedType&reset=true")
                        }
                    )
                }

                composable(
                    "select_contacts/{groupId}/{groupName}/{groupType}",
                    arguments = listOf(
                        navArgument("groupId") { type = NavType.StringType },
                        navArgument("groupName") { type = NavType.StringType },
                        navArgument("groupType") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                    SelectContactsScreen(
                        groupId = groupId,
                        onBack = { navController.popBackStack() },
                        onConfirmSelection = { selectedMembers ->
                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedMembers", selectedMembers)
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    "add_expense?groupId={groupId}&groupName={groupName}&groupType={groupType}&reset={reset}",
                    arguments = listOf(
                        navArgument("groupId") { type = NavType.StringType },
                        navArgument("groupName") { type = NavType.StringType },
                        navArgument("groupType") { type = NavType.StringType },
                        navArgument("reset") {
                            type = NavType.StringType
                            defaultValue = "false"
                        }
                    )
                ) { backStackEntry ->
                    val reset = backStackEntry.arguments?.getString("reset") == "true"
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                    val groupName = Uri.decode(backStackEntry.arguments?.getString("groupName") ?: "")
                    val groupType = Uri.decode(backStackEntry.arguments?.getString("groupType") ?: "")

                    AddExpenseScreen(
                        navController = navController,
                        onBack = { navController.popBackStack() },
                        onSave = { val encodedName = Uri.encode(groupName)
                            val encodedType = Uri.encode(groupType)
                            navController.navigate("group_detail/$groupId/$encodedName/$encodedType") {
                                popUpTo("home") { inclusive = false } // optional backstack cleanup
                            } },
                        groupId = groupId,
                        groupName = groupName,
                        groupType = groupType,
                        viewModel = expenseFlowViewModel,
                        shouldReset = reset
                    )
                }

                composable(
                    "who_paid?groupId={groupId}&groupName={groupName}&groupType={groupType}",
                    arguments = listOf(
                        navArgument("groupId") { type = NavType.StringType },
                        navArgument("groupName") { type = NavType.StringType },
                        navArgument("groupType") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val previousHandle = navController.previousBackStackEntry?.savedStateHandle
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                    val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                    val groupType = backStackEntry.arguments?.getString("groupType") ?: ""

                    val members = previousHandle?.get<List<User>>("selectedMembers") ?: emptyList()
                    val totalAmount = previousHandle?.get<Double>("totalAmount") ?: 0.0
                    val expenseId = previousHandle?.get<String>("expenseId") ?: ""

                    WhoPaidScreen(
                        navController = navController,
                        members = members,
                        totalAmount = totalAmount,
                        groupId = groupId,
                        groupName = groupName,
                        groupType = groupType,
                        onBack = { navController.popBackStack() },
                        expenseId = expenseId,
                        viewModel = expenseFlowViewModel
                    )
                }

                composable(
                    "enter_paid_amount/{groupId}/{groupName}/{groupType}",
                    arguments = listOf(
                        navArgument("groupId") { type = NavType.StringType },
                        navArgument("groupName") { type = NavType.StringType },
                        navArgument("groupType") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val previousHandle = navController.previousBackStackEntry?.savedStateHandle
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                    val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                    val groupType = backStackEntry.arguments?.getString("groupType") ?: ""

                    val members = previousHandle?.get<List<User>>("enterPaidMembers") ?: emptyList()
                    val totalAmount = previousHandle?.get<Double>("enterPaidAmount") ?: 0.0
                    val expenseId = previousHandle?.get<String>("expenseId") ?: ""

                    EnterPaidAmountsScreen(
                        members = members,
                        totalExpenseAmount = totalAmount,
                        groupId = groupId,
                        groupName = groupName,
                        groupType = groupType,
                        expenseId = expenseId,
                        navController = navController,
                        onBack = { navController.popBackStack() },
                        viewModel = expenseFlowViewModel
                    )
                }

                composable(
                    "adjust_split?groupId={groupId}&groupName={groupName}&groupType={groupType}",
                    arguments = listOf(
                        navArgument("groupId") { type = NavType.StringType },
                        navArgument("groupName") { type = NavType.StringType },
                        navArgument("groupType") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val context = LocalContext.current
                    val members by expenseFlowViewModel.selectedMembers.collectAsState()
                    val totalAmount by expenseFlowViewModel.totalAmount.collectAsState()

                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                    val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                    val groupType = backStackEntry.arguments?.getString("groupType") ?: ""

                    val previousHandle = navController.previousBackStackEntry?.savedStateHandle
                    val expenseId = previousHandle?.get<String>("expenseId") ?: ""
                    val paidByMap = previousHandle?.get<Map<String, Double>>("whoPaid")
                    val paidById = previousHandle?.get<String>("selectedPayerId") ?: ""
                    val expenseTitle = previousHandle?.get<String>("expenseTitle") ?: ""

                    AdjustSplitScreen(
                        navController = navController,
                        people = members,
                        groupId = groupId,
                        groupName = groupName,
                        groupType = groupType,
                        totalAmount = totalAmount,
                        onDone = { splitMap ->
                            if (groupId.isBlank() || splitMap.isEmpty()) return@AdjustSplitScreen

                            val expense = mutableMapOf<String, Any>(
                                "expenseId" to expenseId,
                                "title" to expenseTitle,
                                "amount" to totalAmount,
                                "groupId" to groupId,
                                "splitBetween" to splitMap,
                                "timestamp" to System.currentTimeMillis()
                            )

                            if (paidByMap != null) expense["paidBy"] = paidByMap
                            else expense["paidById"] = paidById

                            val database = FirebaseDatabase.getInstance().reference
                            val expenseRef = database.child("groups").child(groupId).child("expenses").child(expenseId)

                            expenseRef.setValue(expense)
                                .addOnSuccessListener {
                                    val encodedGroupName = Uri.encode(groupName)
                                    val encodedGroupType = Uri.encode(groupType)
                                    navController.navigate("add_expense?groupId=$groupId&groupName=$encodedGroupName&groupType=$encodedGroupType&reset=false") {
                                        popUpTo("adjust_split?groupId=$groupId&groupName=$encodedGroupName&groupType=$encodedGroupType") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to save expense", Toast.LENGTH_LONG).show()
                                }
                        },
                        viewModel = expenseFlowViewModel
                    )
                }

                composable(
                    "expense_summary/{groupId}/{groupName}/{groupType}",
                    arguments = listOf(
                        navArgument("groupId") { type = NavType.StringType },
                        navArgument("groupName") { type = NavType.StringType },
                        navArgument("groupType") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                    val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                    val groupType = backStackEntry.arguments?.getString("groupType") ?: ""

                    ExpenseSummaryScreen(
                        navController = navController,
                        viewModel = expenseFlowViewModel,
                        groupId = groupId,
                        groupName = groupName,
                        groupType = groupType
                    )
                }

                composable("contact_picker") {
                    ContactPickerScreen(navController = navController)
                }
                composable(
                    route = "friend_detail/{friendUid}/{friendName}",
                    arguments = listOf(
                        navArgument("friendUid") { type = NavType.StringType },
                        navArgument("friendName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val friendUid = backStackEntry.arguments?.getString("friendUid") ?: ""
                    val friendName = backStackEntry.arguments?.getString("friendName") ?: ""
                    FriendDetailScreen(friendUid = friendUid, friendName = friendName, navController = navController)
                }
                composable(
                    route = "friend_add_expense/{friendUid}/{friendName}",
                    arguments = listOf(
                        navArgument("friendUid") { type = NavType.StringType },
                        navArgument("friendName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val friendUid = backStackEntry.arguments?.getString("friendUid") ?: ""
                    val friendName = backStackEntry.arguments?.getString("friendName") ?: ""

                    FriendAddExpenseScreen(
                        friendUid = friendUid,
                        friendName = friendName,
                        navController = navController
                    )
                }
                composable(
                    route = "who_paid_friend/{friendUid}/{friendName}/{totalAmount}",
                    arguments = listOf(
                        navArgument("friendUid") { type = NavType.StringType },
                        navArgument("friendName") { type = NavType.StringType },
                        navArgument("totalAmount") { type = NavType.FloatType }
                    )
                ) { backStackEntry ->
                    val friendUid = backStackEntry.arguments?.getString("friendUid") ?: return@composable
                    val friendName = backStackEntry.arguments?.getString("friendName") ?: return@composable
                    val totalAmount = backStackEntry.arguments?.getFloat("totalAmount") ?: 0f

                    val viewModel: FriendExpenseViewModel = viewModel()
                    val currentUser = viewModel.currentUser.copy(
                        name = if (viewModel.currentUser.name.isBlank()) "You" else viewModel.currentUser.name
                    )
                    val friend = User(uid = friendUid, name = friendName, email = "")

                    WhoPaidFriendScreen(
                        currentUser = currentUser,
                        friend = friend,
                        selectedPayerId = viewModel.paidBy.collectAsState().value ?: currentUser.uid,
                        onBack = { navController.popBackStack() },
                        onDone = { payerId ->
                            viewModel.setPaidBy(payerId)
                            navController.popBackStack()
                        },
                        onMultiplePeopleClick = {
                            val encodedName = Uri.encode(friend.name)
                            navController.navigate("enter_paid_amount_friend/${friend.uid}/$encodedName/$totalAmount")
                        }
                    )
                }

                composable(
                    route = "enter_paid_amount_friend/{friendUid}/{friendName}/{totalAmount}",
                    arguments = listOf(
                        navArgument("friendUid") { type = NavType.StringType },
                        navArgument("friendName") { type = NavType.StringType },
                        navArgument("totalAmount") { type = NavType.FloatType }
                    )
                ) { backStackEntry ->
                    val friendUid = backStackEntry.arguments?.getString("friendUid") ?: return@composable
                    val friendName = backStackEntry.arguments?.getString("friendName") ?: return@composable
                    val totalAmount = backStackEntry.arguments?.getFloat("totalAmount")?.toDouble() ?: 0.0

                    val viewModel: FriendExpenseViewModel = viewModel()
                    val currentUser = viewModel.currentUser
                    val friend = User(uid = friendUid, name = friendName, email = "")
                    val participants = listOf(currentUser, friend)

                    EnterPaidAmountsFriendScreen(
                        participants = participants,
                        totalAmount = totalAmount,
                        onBack = { navController.popBackStack() },
                        onConfirm = { paidMap ->
                            viewModel.setPaidAmounts(paidMap)
                            navController.popBackStack()
                        },
                        currentUser = currentUser
                    )
                }
                composable(
                    route = "adjust_split_friend/{uids}",
                    arguments = listOf(navArgument("uids") { type = NavType.StringType })
                ) { backStackEntry ->
                    val uids = backStackEntry.arguments?.getString("uids")?.split(",") ?: emptyList()
                    val viewModel: FriendAdjustSplitViewModel = viewModel(
                        factory = FriendAdjustSplitViewModelFactory(uids)
                    )
                    val participants by viewModel.participants.collectAsState()

                    // Other data can come from savedStateHandle
                    val totalAmount = navController.previousBackStackEntry
                        ?.savedStateHandle?.get<Float>("totalAmount") ?: 0f
                    val paidByUser = navController.previousBackStackEntry
                        ?.savedStateHandle?.get<User>("paidByUser") ?: User("", "Unknown", "")

                    FriendAdjustSplitScreen(
                        participants = participants,
                        totalAmount = totalAmount,
                        paidByUser = paidByUser,
                        onBack = { navController.popBackStack() },
                        onConfirm = { splitMap ->
                            navController.popBackStack()
                        }
                    )
                }

            }
        }
    }
}