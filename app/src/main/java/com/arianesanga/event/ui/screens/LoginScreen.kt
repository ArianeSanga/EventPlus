package com.arianesanga.event.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.repository.UserRepository
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.UserRemoteRepository
import com.arianesanga.event.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authService: AuthService = AuthService(),
    userRepository: UserRepository = UserRepository(
        AppDatabase.getInstance(LocalContext.current).userDao(),
        UserRemoteRepository()
    )
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = WHITE)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(DARKBLUE, MEDIUMBLUE, DARKBLUE)))
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("LOGIN", color = WHITE, fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("EMAIL", color = WHITE.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = LIGHTBLUE) },
                textStyle = LocalTextStyle.current.copy(color = WHITE),
                modifier = Modifier.fillMaxWidth().background(Color(0x22FFFFFF), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("SENHA", color = WHITE.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = LIGHTBLUE) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = LIGHTBLUE
                        )
                    }
                },
                textStyle = LocalTextStyle.current.copy(color = WHITE),
                modifier = Modifier.fillMaxWidth().background(Color(0x22FFFFFF), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true

                    authService.loginUser(
                        email = email,
                        password = password,
                        onSuccess = {
                            isLoading = false
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onError = {
                            isLoading = false
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LIGHTBLUE),
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                if (isLoading)
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WHITE)
                else
                    Text("ENTRAR", color = WHITE)
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text("Esqueceu a senha? ", color = WHITE)
                Text(
                    "Clique aqui",
                    color = LIGHTBLUE,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        navController.navigate("forgot_password")
                    }
                )
            }
        }
    }
}