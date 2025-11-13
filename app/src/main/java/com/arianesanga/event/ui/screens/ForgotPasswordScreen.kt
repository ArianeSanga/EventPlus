package com.arianesanga.event.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.arianesanga.event.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {

    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Senha") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = WHITE)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DARKBLUE,
                    titleContentColor = WHITE
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(listOf(DARKBLUE, MEDIUMBLUE, DARKBLUE))
                )
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Digite seu e-mail para redefinir sua senha",
                color = WHITE,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Seu e-mail", color = WHITE.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = "Email",
                        tint = LIGHTBLUE
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x22FFFFFF), shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0x33FFFFFF),
                    unfocusedContainerColor = Color(0x22FFFFFF),
                    cursorColor = LIGHTBLUE,
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isBlank()) {
                        Toast.makeText(context, "Digite um e-mail válido.", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "E-mail de redefinição enviado com sucesso!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.popBackStack() // volta para LoginScreen
                                } else {
                                    val error = task.exception?.message ?: "Erro ao enviar o e-mail."
                                    Toast.makeText(
                                        context,
                                        "Falha: $error",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LIGHTBLUE),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = WHITE,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(25.dp)
                    )
                } else {
                    Text(
                        text = "ENVIAR LINK",
                        color = WHITE,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}