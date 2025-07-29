import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.splitmoney.R
import android.util.Patterns
import androidx.navigation.compose.rememberNavController
import com.example.splitmoney.Authentication.AuthViewModel


// --- Define Your Colors ---
val SplitwiseTeal = Color(0xFF3DAB94)
val SplitwiseGrey = Color(0xFF5A5A5A)
val LightGrey = Color(0xFFEDEDED)
val ForgotTextGrey = Color(0xFF757575)

@Composable
fun LoginScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()
    val loginState by viewModel.loginState
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        if (loginState == "Success") {
            navController.navigate("home_screen") {
                popUpTo("login_screen") { inclusive = true }
            }
        } else if (loginState != null) {
            Toast.makeText(context, loginState, Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {

        // Background Teal Shape
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(TealShape)
            .background(SplitwiseTeal))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo), // Replace with your logo
                    contentDescription = "Logo",
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SPLITWISE",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LOGIN",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* Active */ }
                )
                Text(
                    text = "SIGN UP",
                    color = SplitwiseGrey,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.clickable { /* Navigate */ }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Card with Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                // Email Field
                Text(text = "Email Address", color = Color.Black, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                var email by remember { mutableStateOf("") }
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Enter email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightGrey, RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = LightGrey,
                        focusedContainerColor = LightGrey,
                        disabledContainerColor = LightGrey,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                    ,
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                Text(text = "Password", color = Color.Black, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                var password by remember { mutableStateOf("") }
                var passwordVisible by remember { mutableStateOf(false) }
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightGrey, RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = LightGrey,
                        focusedContainerColor = LightGrey,
                        disabledContainerColor = LightGrey,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                    ,
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) painterResource(id = android.R.drawable.ic_menu_view)
                        else painterResource(id = android.R.drawable.ic_menu_close_clear_cancel)
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(painter = icon, contentDescription = null)
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Forgot your password?",
                    color = ForgotTextGrey,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.End).clickable { /* TODO */ }
                )
                Spacer(modifier = Modifier.height(24.dp))

                val isLoading = viewModel.isLoading.value
                val navController = rememberNavController()
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                        } else if (password.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.loginUser(email, password, navController)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Login")
                    }
                }
            }
        }
    }

}

// Custom Curved Teal Shape
val TealShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.7f)
    quadraticBezierTo(
        size.width * 0.5f,
        size.height * 1.2f, // controls how low the curve dips
        0f,
        size.height * 0.7f
    )
    close()
}

