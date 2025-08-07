package com.example.splitmoney

import LoginScreen
import User
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.example.splitmoney.friendIndividualhome.FriendDetailViewModel
import com.example.splitmoney.friendIndividualhome.FriendDetailViewModelFactory
import com.example.splitmoney.friendSummary.FriendExpenseSummaryScreen
import com.example.splitmoney.friendWhoPaid.EnterPaidAmountsFriendScreen
import com.example.splitmoney.friendWhoPaid.WhoPaidFriendScreen
import com.example.splitmoney.groupindividualhome.GroupDetailScreen
import com.example.splitmoney.groupindividualhome.GroupDetailViewModel
import com.example.splitmoney.groupindividualhome.GroupDetailViewModelFactory
import com.example.splitmoney.signupLogin.AuthChoiceScreen
import com.example.splitmoney.signupLogin.SignUpScreen
import com.example.splitmoney.signupLogin.SplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
const val FRIEND_ADD_EXPENSE_ROUTE = "friend_add_expense"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.google.firebase.FirebaseApp.initializeApp(this)

        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val expenseFlowViewModel: ExpenseFlowViewModel = viewModel()
            val friendExpenseViewModel: FriendExpenseViewModel = viewModel()


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
                        expenseFlowViewModel = expenseFlowViewModel,
                        onBack = { navController.popBackStack() },
                        onAddMembers = {
                            val encodedName = Uri.encode(groupName)
                            val encodedType = Uri.encode(groupType)
                            navController.navigate("select_contacts/$groupId/$encodedName/$encodedType")
                        },
                        onAddExpense = {
                            val members = viewModel.members.value
                            navController.currentBackStackEntry?.savedStateHandle?.set("selectedMembers", members)
                            expenseFlowViewModel.setSelectedMembers(members)

                            val encodedName = Uri.encode(groupName)
                            val encodedType = Uri.encode(groupType)
                            navController.navigate("add_expense?groupId=$groupId&groupName=$encodedName&groupType=$encodedType&reset=true")
                        },
                        onShareGroupLink = {}
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
                        onSave = {
                            val encodedName = Uri.encode(groupName)
                            val encodedType = Uri.encode(groupType)
                            navController.navigate("group_detail/$groupId/$encodedName/$encodedType") {
                                popUpTo("home") { inclusive = false }
                            }
                        },
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


                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                    val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                    val groupType = backStackEntry.arguments?.getString("groupType") ?: ""


                    val previousEntry = navController.previousBackStackEntry
                    val savedStateHandle = previousEntry?.savedStateHandle

                    val members = savedStateHandle?.get<List<User>>("enterPaidMembers") ?: emptyList()
                    val totalAmount = savedStateHandle?.get<Double>("enterPaidAmount") ?: 0.0
                    val expenseId = savedStateHandle?.get<String>("expenseId") ?: ""

                    EnterPaidAmountsScreen(
                        members = members,
                        totalExpenseAmount = totalAmount,
                        groupId = groupId,
                        groupName = groupName,
                        groupType = groupType,
                        expenseId = expenseId,
                        navController = navController,
                        viewModel = expenseFlowViewModel,
                        onBack = { navController.popBackStack() },
                        onConfirm = { paidMap ->
                            // Save into viewModel
                            expenseFlowViewModel.setPaidBy("multiple")
                            expenseFlowViewModel.setWhoPaidMap(paidMap)

                            // ✅ Navigate to AddExpenseScreen
                            val encodedName = Uri.encode(groupName)
                            val encodedType = Uri.encode(groupType)

                            navController.navigate(
                                "add_expense?groupId=$groupId&groupName=$encodedName&groupType=$encodedType&reset=false"
                            ) {
                                // Optional: Avoid multiple copies of the destination
                                popUpTo("add_expense") { inclusive = true }
                            }
                        }
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
                    Log.d("NAV", "Navigated to FriendDetail Composable. UID = $friendUid, NAME = $friendName")
                    FriendDetailScreen(friendUid = friendUid, friendName = friendName, navController = navController)
                }

                composable(
                    route = "$FRIEND_ADD_EXPENSE_ROUTE/{friendUid}/{friendName}",
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
                        navController = navController,
                        viewModel = friendExpenseViewModel
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

                    val currentUser = friendExpenseViewModel.currentUser.copy(
                        name = if (friendExpenseViewModel.currentUser.name.isBlank()) "You" else friendExpenseViewModel.currentUser.name
                    )
                    val friend = User(uid = friendUid, name = friendName, email = "")

                    WhoPaidFriendScreen(
                        navController = navController,
                        currentUser = currentUser,
                        friend = friend,
                        totalAmount = totalAmount,
                        selectedPayerId = friendExpenseViewModel.paidByUserIds.firstOrNull() ?: currentUser.uid,
                        viewModel = friendExpenseViewModel,
                        onBack = { navController.popBackStack() },
                        onDone = { navController.popBackStack() },
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

                    val currentUser = friendExpenseViewModel.currentUser
                    val friend = User(uid = friendUid, name = friendName, email = "")
                    val participants = listOf(currentUser, friend)

                    EnterPaidAmountsFriendScreen(
                        participants = participants,
                        totalAmount = totalAmount,
                        currentUser = currentUser,
                        navController = navController,
                        viewModel = friendExpenseViewModel,
                        onBack = {
                            val encodedName = Uri.encode(friendName)
                            navController.popBackStack(
                                route = "friend_add_expense/$friendUid/$encodedName",
                                inclusive = false
                            )
                        },
                        friendUid = friendUid,
                        friendName = friendName
                    )
                }

                composable(
                    route = "adjust_split_friend/{uids}",
                    arguments = listOf(navArgument("uids") { type = NavType.StringType })
                ) { backStackEntry ->
                    val uids = backStackEntry.arguments?.getString("uids")?.split("-") ?: emptyList()
                    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
                    val friendUid = uids.find { it != currentUserUid } ?: ""

                    val prevEntry = navController.previousBackStackEntry
                    val friendName = prevEntry?.savedStateHandle?.get<String>("friendName") ?: "Unknown"
                    val totalAmount = prevEntry?.savedStateHandle?.get<Float>("totalAmount") ?: 0f
                    val paidByUser = prevEntry?.savedStateHandle?.get<User>("paidByUser") ?: User("", "Unknown", "")

                    val friendAdjustViewModel: FriendAdjustSplitViewModel = viewModel(
                        factory = FriendAdjustSplitViewModelFactory(uids)
                    )
                    val participants by friendAdjustViewModel.participants.collectAsState()

                    FriendAdjustSplitScreen(
                        participants = participants,
                        totalAmount = totalAmount,
                        paidByUser = paidByUser,
                        viewModel = friendExpenseViewModel,
                        onBack = { navController.popBackStack() },
                        onConfirmNavigate = {
                            navController.navigate("friend_expense_summary/$friendUid/${Uri.encode(friendName)}")
                        }
                    )
                }

                composable(
                    route = "friend_expense_summary/{friendUid}/{friendName}",
                    arguments = listOf(
                        navArgument("friendUid") { type = NavType.StringType },
                        navArgument("friendName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val friendUid = backStackEntry.arguments?.getString("friendUid") ?: return@composable
                    val friendName = backStackEntry.arguments?.getString("friendName") ?: "Unknown"

                    FriendExpenseSummaryScreen(
                        friendName = friendName,
                        friendUid = friendUid,
                        viewModel = friendExpenseViewModel,
                        navController = navController,
                        onBack = { navController.popBackStack() }
                    )
                }

            }
        }
    }
}