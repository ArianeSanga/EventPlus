package com.arianesanga.event.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.arianesanga.event.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (email: String, password: String, onFinish: () -> Unit) -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = WHITE)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // transparente
                    titleContentColor = WHITE,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(DARKBLUE, MEDIUMBLUE, DARKBLUE)
                    )
                )
                .padding(contentPadding)
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LOGIN",
                color = WHITE,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // === CAMPO EMAIL ===
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("EMAIL", color = WHITE.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Ícone de e-mail",
                        tint = LIGHTBLUE
                    )
                },
                textStyle = LocalTextStyle.current.copy(color = WHITE),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x22FFFFFF), shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0x33FFFFFF),
                    unfocusedContainerColor = Color(0x22FFFFFF),
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = LIGHTBLUE,
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === CAMPO SENHA ===
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("SENHA", color = WHITE.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = "Ícone de senha",
                        tint = LIGHTBLUE
                    )
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Mostrar/Ocultar senha", tint = LIGHTBLUE)
                    }
                },
                textStyle = LocalTextStyle.current.copy(color = WHITE),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x22FFFFFF), shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0x33FFFFFF),
                    unfocusedContainerColor = Color(0x22FFFFFF),
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = LIGHTBLUE,
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE
                ),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // BOTÃO DE LOGIN
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        onLogin(email, password) {
                            isLoading = false
                        }
                    } else {
                        Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LIGHTBLUE),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = WHITE,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text("ENTRAR COM E-MAIL", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WHITE)
                }
            }

            // === TEXTO "ESQUECEU A SENHA?" ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Esqueceu a senha? ",
                    color = WHITE,
                    fontSize = 15.sp
                )
                Text(
                    text = "Clique aqui",
                    color = LIGHTBLUE,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onForgotPassword() }
                )
            }
        }
    }
}