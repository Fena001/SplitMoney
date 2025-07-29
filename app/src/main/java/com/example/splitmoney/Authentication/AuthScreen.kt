package com.example.splitmoney.Authentication

import LoginScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.splitmoney.signupLogin.HeaderTabs
import com.example.splitmoney.signupLogin.SignUpScreen

@Composable
fun AuthScreen(navController: NavHostController) {
    var isLogin by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with logo + toggle
        HeaderTabs(isLogin) { selected ->
            isLogin = selected
        }

        // Form
        if (isLogin) {
            LoginScreen(navController)
        } else {
            SignUpScreen(navController)
        }
    }
}
