package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arianesanga.event.ui.activities.HomeActivity
import com.arianesanga.event.ui.theme.BROWN900
import com.arianesanga.event.ui.theme.GRAY900
import com.arianesanga.event.ui.theme.ORANGE
import com.arianesanga.event.ui.theme.WHITE
import com.arianesanga.event.ui.theme.EventTheme // Importando seu tema
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailLoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acesso com E-mail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = WHITE)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GRAY900,
                    titleContentColor = WHITE
                )
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GRAY900, BROWN900, GRAY900)
                    )
                )
                .padding(contentPadding)
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Entre na sua conta",
                color = WHITE,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // CAMPO EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Seu Email") },
                textStyle = LocalTextStyle.current.copy(color = WHITE),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ORANGE,
                    unfocusedBorderColor = ORANGE.copy(alpha = 0.6f),
                    focusedLabelColor = ORANGE,
                    unfocusedLabelColor = WHITE.copy(alpha = 0.6f),
                    cursorColor = ORANGE,
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CAMPO SENHA
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Sua Senha") },
                textStyle = LocalTextStyle.current.copy(color = WHITE),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ORANGE,
                    unfocusedBorderColor = ORANGE.copy(alpha = 0.6f),
                    focusedLabelColor = ORANGE,
                    unfocusedLabelColor = WHITE.copy(alpha = 0.6f),
                    cursorColor = ORANGE,
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE
                ),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, "Mostrar/Ocultar senha", tint = ORANGE)
                    }
                }
            )

            // Link "Esqueceu a Senha"
            Text(
                text = "Esqueceu a senha?",
                color = ORANGE,
                fontSize = 14.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable {
                        Toast.makeText(context, "Funcionalidade de recuperação não implementada aqui.", Toast.LENGTH_SHORT).show()
                    }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // BOTÃO DE LOGIN
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        onLogin(email, password)
                    } else {
                        Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ORANGE),
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
                    Text("Entrar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WHITE)
                }
            }
        }
    }
}

class EmailLoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            EventTheme { // Usando o tema do seu projeto
                EmailLoginScreen(
                    onLogin = ::firebaseAuthWithEmailPassword,
                    onBack = ::finish
                )
            }
        }
    }

    private fun firebaseAuthWithEmailPassword(email: String, password: String) {
        // Esta Activity gerencia o estado de login
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    val error = task.exception?.message ?: "Erro desconhecido ao logar."
                    Toast.makeText(this, "Falha no Login: $error", Toast.LENGTH_LONG).show()
                }
                // Não precisa resetar o loading aqui, pois o Composable fará isso após a Activity voltar/ser fechada,
                // ou se você implementar um ViewModel. No código atual, a Activity é fechada após o sucesso.
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finishAffinity() // Fecha todas as activities de login/cadastro
    }
}