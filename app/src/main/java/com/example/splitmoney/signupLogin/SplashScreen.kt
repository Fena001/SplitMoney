package com.example.splitmoney.signupLogin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.splitmoney.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        FirebaseApp.initializeApp(context)
        delay(2000)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

            userRef.get().addOnSuccessListener { snapshot ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (snapshot.exists()) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("signup") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }.addOnFailureListener {
                CoroutineScope(Dispatchers.Main).launch {
                    navController.navigate("auth_choice") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        } else {
            navController.navigate("auth_choice") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(id = R.drawable.logo), contentDescription = "Logo")
            Spacer(modifier = Modifier.height(16.dp))
            Text("SPLIITWISE", color = Color(0xFF49B69C), fontSize = 24.sp)
            Text("Split bills the easy way", color = Color.Gray, fontSize = 14.sp)
        }
    }
}