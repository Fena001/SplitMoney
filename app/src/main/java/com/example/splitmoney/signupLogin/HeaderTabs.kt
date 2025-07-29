package com.example.splitmoney.signupLogin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitmoney.R

@Composable
fun HeaderTabs(isLogin: Boolean, onTabSelected: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF49B69C)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier.size(64.dp)
        )
        Text("SPLITWISE", color = Color.White, fontSize = 24.sp)

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "LOGIN",
                color = if (isLogin) Color.White else Color.LightGray,
                fontSize = 18.sp,
                modifier = Modifier.clickable { onTabSelected(true) }
            )
            Text(
                text = "SIGN UP",
                color = if (!isLogin) Color.White else Color.LightGray,
                fontSize = 18.sp,
                modifier = Modifier.clickable { onTabSelected(false) }
            )
        }
    }
}
