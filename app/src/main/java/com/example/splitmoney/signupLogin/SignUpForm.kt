package com.example.splitmoney.signupLogin

import User
import android.R.attr.name
import android.R.attr.phoneNumber
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.splitmoney.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SignUpScreen(
    navController: NavController
) {
    val context = LocalContext.current

    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(bottomStart = 100.dp, bottomEnd = 100.dp))
                .background(Color(0xFF00897B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SPLITWISE",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "SIGN UP",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LOGIN",
                        color = Color.LightGray,
                        fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            navController.navigate("login")
                        }
                    )
                }
            }
        }

        // Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 220.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                InputField("Name", nameInput) { nameInput = it }
                Spacer(modifier = Modifier.height(10.dp))
                InputField("Email Address", emailInput) { emailInput = it }
                Spacer(modifier = Modifier.height(10.dp))
                InputField("Password", passwordInput, isPassword = true) { passwordInput = it }
                Spacer(modifier = Modifier.height(10.dp))
                InputField("Phone Number", phoneInput) { phoneInput = it }

                Spacer(modifier = Modifier.height(20.dp))

                val context = LocalContext.current

                Button(onClick = {
                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(emailInput, passwordInput)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener

                                val user = User(
                                    uid = uid,
                                    name = nameInput,
                                    email = emailInput,
                                    phoneNumber = phoneInput,
                                    friends = emptyMap(),
                                    groups = emptyMap()
                                )

                                FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Signup successful!", Toast.LENGTH_SHORT).show()
                                        // navigate to home or wherever
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to save user: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                Toast.makeText(context, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }) {
                    Text("Sign Up")
                }

            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = label) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color(0xFFF5F5F5),
                disabledContainerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

fun saveUserToDatabase(uid: String, name: String, email: String) {
    val user = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "friends" to emptyList<String>(),
        "groups" to emptyList<String>()
    )

    FirebaseDatabase.getInstance()
        .getReference("users")
        .child(uid)
        .setValue(user)
        .addOnSuccessListener {
            Log.d("Firebase", "User saved to database")
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error saving user to database", e)
        }
}