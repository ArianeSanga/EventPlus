package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.ui.screens.CadastroScreen
import com.google.firebase.auth.FirebaseAuth

class CadastroActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            EventTheme {
                CadastroScreen(
                    onRegisterClick = ::registerUserWithEmailPassword,
                    onNavigateToLogin = { finish() } // Fecha a tela e volta para a MainActivity (Login)
                )
            }
        }
    }

    /**
     * Tenta registrar um novo usuário no Firebase.
     */
    private fun registerUserWithEmailPassword(email: String, password: String) {
        // Validação básica
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()

                    // Navega para a Home
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Erro desconhecido ao cadastrar."
                    Toast.makeText(this, "Falha no Cadastro: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}