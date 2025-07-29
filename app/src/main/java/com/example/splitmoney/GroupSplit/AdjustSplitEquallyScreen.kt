import User
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitmoney.AddExpence.ExpenseFlowViewModel


@Composable
fun SplitEquallyTab(
    viewModel: ExpenseFlowViewModel,
    members: List<User>,
    selectedMembers: SnapshotStateMap<String, Boolean>,
    perPersonAmount: Double,
    onToggleAll: () -> Unit,
    onDone: () -> Unit = {}, // optional for now
    onSplitChanged: (Map<String, Double>) -> Unit
) {
    val selectedCount = selectedMembers.count { it.value }
    val allSelected = selectedMembers.values.all { it }

    LaunchedEffect(Unit) {
        viewModel.updateSplitType("equally")
    }


    // Automatically recalculate the split when selection changes
    LaunchedEffect(selectedMembers.toMap()) {
        val selected = members.filter { selectedMembers[it.uid] == true }
        val perPerson = if (selected.isNotEmpty()) perPersonAmount else 0.0
        val updatedMap = selected.associate { it.uid to perPerson }
        onSplitChanged(updatedMap)
    }

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {

        Text(
            text = "Split equally",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = "Select which people owe an equal share.",
            color = Color.LightGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 16.dp)
        ) {
            items(members) { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Maroon Circle Avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF800000))
                    ) {
                        Text(
                            text = member.name.ifBlank { "?" }.first().uppercaseChar().toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = member.name.ifBlank { "You" },
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Checkbox(
                        checked = selectedMembers.getOrDefault(member.uid, false),
                        onCheckedChange = { selectedMembers[member.uid] = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50),
                            uncheckedColor = Color.White
                        )
                    )
                }
            }
        }

        // Bottom summary row
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF212121))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "₹${"%.2f".format(perPersonAmount)}/person",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "($selectedCount people)",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (allSelected) "None" else "All",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { onToggleAll() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4CAF50),
                        uncheckedColor = Color.White
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSplitEquallyTab() {
    val dummyUsers = listOf(
        User(uid = "1", name = "Fena", email = "fena@example.com"),
        User(uid = "2", name = "Beena", email = "beena@example.com"),
        User(uid = "3", name = "Zimmy", email = "zimmy@example.com")
    )

    val selectedMembers = remember {
        mutableStateMapOf(
            "1" to true,
            "2" to true,
            "3" to true
        )
    }

    val perPersonAmount = 150.0 / selectedMembers.count { it.value }

//    MaterialTheme {
//        Surface(color = Color.Black) {
//            SplitEquallyTab(
//                members = dummyUsers,
//                selectedMembers = selectedMembers,
//                perPersonAmount = perPersonAmount,
//                onToggleAll = {
//                    val allSelected = selectedMembers.values.all { it }
//                    dummyUsers.forEach { selectedMembers[it.uid] = !allSelected }
//                }
//            )
//        }
//    }
}
