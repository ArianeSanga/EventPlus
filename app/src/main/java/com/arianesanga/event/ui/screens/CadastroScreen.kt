package com.arianesanga.event.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arianesanga.event.ui.theme.BROWN900
import com.arianesanga.event.ui.theme.GRAY900
import com.arianesanga.event.ui.theme.ORANGE
import com.arianesanga.event.ui.theme.WHITE

@Composable
fun CadastroScreen(
    onRegisterClick: (email: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GRAY900, BROWN900, GRAY900, GRAY900)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Novo Cadastro",
            color = WHITE,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 30.dp)
        )

        // --- Campo Email ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            textStyle = LocalTextStyle.current.copy(color = WHITE),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ORANGE,
                unfocusedBorderColor = ORANGE,
                focusedLabelColor = ORANGE,
                unfocusedLabelColor = ORANGE,
                cursorColor = ORANGE
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        // --- Campo Senha ---
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Senha (mínimo 6 caracteres)") },
            textStyle = LocalTextStyle.current.copy(color = WHITE),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ORANGE,
                unfocusedBorderColor = ORANGE,
                focusedLabelColor = ORANGE,
                unfocusedLabelColor = ORANGE,
                cursorColor = ORANGE
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        // --- Campo Confirmar Senha ---
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordError = null
            },
            label = { Text("Confirmar Senha") },
            textStyle = LocalTextStyle.current.copy(color = WHITE),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (passwordError != null) Color.Red else ORANGE,
                unfocusedBorderColor = if (passwordError != null) Color.Red else ORANGE,
                focusedLabelColor = if (passwordError != null) Color.Red else ORANGE,
                unfocusedLabelColor = if (passwordError != null) Color.Red else ORANGE,
                cursorColor = ORANGE
            )
        )

        // Mensagem de erro de senhas diferentes
        if (passwordError != null) {
            Text(
                text = passwordError!!,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- Botão de Cadastro ---
        Button(
            onClick = {
                if (password != confirmPassword) {
                    passwordError = "As senhas não coincidem!"
                } else {
                    passwordError = null
                    onRegisterClick(email, password)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ORANGE),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(50), spotColor = ORANGE)
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text(
                text = "CADASTRAR",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WHITE
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Link para o Login ---
        TextButton(onClick = onNavigateToLogin) {
            Text(
                text = "Já tem uma conta? Faça Login",
                color = ORANGE,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}