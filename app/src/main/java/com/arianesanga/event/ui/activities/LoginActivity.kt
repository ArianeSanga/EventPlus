package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arianesanga.event.ui.screens.LoginScreen
import com.arianesanga.event.ui.theme.EventTheme
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            EventTheme {
                LoginScreen(
                    onLogin = { email, password, onFinish ->
                        firebaseAuthWithEmailPassword(email, password, onFinish)
                    },
                    onBack = ::finish,
                    onForgotPassword = { navigateToForgotPassword() }
                )
            }
        }
    }

    private fun firebaseAuthWithEmailPassword(
        email: String,
        password: String,
        onFinish: () -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                onFinish() // ✅ Para o loading
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    val error = task.exception?.message ?: "Erro desconhecido ao logar."
                    Toast.makeText(this, "Falha no Login: e-mail ou senha incorretos", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }
}