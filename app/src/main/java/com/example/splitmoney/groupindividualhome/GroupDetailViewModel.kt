package com.example.splitmoney.groupindividualhome

import Group
import User
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitmoney.dataclass.BalanceSummary
import com.example.splitmoney.dataclass.Expense
import com.example.splitmoney.dataclass.MemberBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.reflect.Member

class GroupDetailViewModel(
    private val groupId: String,
    private val repository: GroupDetailRepository = GroupDetailRepository()
) : ViewModel() {

    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group

    private val firebaseRef = FirebaseDatabase.getInstance().reference
    private val database = FirebaseDatabase.getInstance().reference

    init {
        loadGroupExpenses()
        loadGroupMembers()
    }

    fun loadGroupMembers() {
        val groupRef =
            FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("members")

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userIds = snapshot.children.mapNotNull { it.key }

                if (userIds.isEmpty()) {
                    _members.value = emptyList()
                    return
                }

                val usersRef = FirebaseDatabase.getInstance().getReference("users")
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        val fetchedUsers = userIds.mapNotNull { uid ->
                            userSnapshot.child(uid).getValue(User::class.java)
                        }
                        _members.value = fetchedUsers
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun loadGroupExpenses() {
        // ❌ Remove this
        // repository.getGroupExpenses(groupId) { expenses ->
        //     _expenses.value = expenses
        // }

        // ✅ Use Firestore instead
        fetchExpensesForGroup(groupId)
    }

    fun fetchMembersFromFirebase(onResult: (List<User>) -> Unit) {
        db.collection("groups").document(groupId).get().addOnSuccessListener { groupSnapshot ->
            val memberIds =
                (groupSnapshot["members"] as? Map<*, *>)?.keys?.mapNotNull { it as? String }
                    ?: emptyList()

            if (memberIds.isEmpty()) {
                onResult(emptyList())
                return@addOnSuccessListener
            }

            db.collection("users")
                .whereIn("uid", memberIds)
                .get()
                .addOnSuccessListener { userSnapshots ->
                    val members = userSnapshots.mapNotNull { it.toObject(User::class.java) }
                    onResult(members)
                }
        }
    }

    private val db = FirebaseFirestore.getInstance()

    fun fetchExpensesForGroup(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups")
            .document(groupId)
            .collection("expenses")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                _expenses.value = list
            }
    }

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members: StateFlow<List<User>> = _members

    private val _balanceSummary = MutableStateFlow<BalanceSummary?>(null)
    val balanceSummary: StateFlow<BalanceSummary?> = _balanceSummary

    // ... your loading code (unchanged)

    /**
     * Computes simplified, netted balances *between the current user and each other member*.
     * Handles multi-payer expenses and per-person split amounts.
     */
    fun calculateBalanceSummary(currentUid: String, members: List<User>, expenses: List<Expense>) {
        val nameOf = members.associateBy({ it.uid }, { it.name })

        // Map of OTHER_UID -> signed amount ( + = they owe YOU, - = you owe THEM )
        val perOther = mutableMapOf<String, Double>()

        expenses.forEach { expense ->
            val paidMap = expense.paidBy           // Map<String, Double> : payer -> amount paid
            val splitMap = expense.splitBetween    // Map<String, Double> : participant -> share amount

            if (paidMap.isNullOrEmpty() || splitMap.isNullOrEmpty()) return@forEach

            // safeguard
            val totalPaid = paidMap.values.sum()   // Double.sum() avoids sumOf ambiguity
            if (totalPaid == 0.0) return@forEach

            // For each participant’s share, apportion to each payer by contribution ratio
            splitMap.forEach { (participantId, participantShare) ->
                paidMap.forEach { (payerId, payerPaid) ->
                    val proportion = payerPaid / totalPaid
                    val transfer = participantShare * proportion  // participant -> payer

                    when {
                        // You are the payer; participant owes you
                        payerId == currentUid && participantId != currentUid -> {
                            perOther[participantId] = (perOther[participantId] ?: 0.0) + transfer
                        }

                        // You are the participant; you owe the payer
                        participantId == currentUid && payerId != currentUid -> {
                            perOther[payerId] = (perOther[payerId] ?: 0.0) - transfer
                        }

                        else -> Unit
                    }
                }
            }
        }

        fun r2(x: Double) = kotlin.math.round(x * 100.0) / 100.0

        val owedToYou = perOther
            .filter { it.key != currentUid && it.value > 0.004 }      // exclude self & tiny noise
            .map { (uid, amt) -> MemberBalance(uid, nameOf[uid] ?: uid, r2(amt)) }
            .sortedByDescending { it.amount }

        val youOwe = perOther
            .filter { it.key != currentUid && it.value < -0.004 }
            .map { (uid, amt) -> MemberBalance(uid, nameOf[uid] ?: uid, r2(-amt)) }
            .sortedByDescending { it.amount }

        val net = r2(owedToYou.sumOf { it.amount } - youOwe.sumOf { it.amount })

        // Keep the list short like Splitwise shows
        val showLimit = 3
        val owedShown = owedToYou.take(showLimit)
        val oweShown = youOwe.take(showLimit)
        val othersCount = (owedToYou.size + youOwe.size - owedShown.size - oweShown.size).coerceAtLeast(0)

        _balanceSummary.value = BalanceSummary(
            netBalance = net,
            topDebtsOwedToYou = owedShown,
            topDebtsYouOwe = oweShown,
            otherBalancesCount = othersCount
        )
    }
}

