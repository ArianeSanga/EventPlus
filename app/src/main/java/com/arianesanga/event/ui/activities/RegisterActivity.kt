package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arianesanga.event.ui.screens.RegisterScreen
import com.arianesanga.event.ui.theme.EventTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            EventTheme {
                RegisterScreen(
                    onRegisterClick = { fullName, username, phone, email, password ->
                        isLoading = true
                        errorMessage = null
                        registerUser(fullName, username, phone, email, password) { success, error ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(
                                    this,
                                    "Cadastro realizado com sucesso!",
                                    Toast.LENGTH_LONG
                                ).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            } else {
                                errorMessage = error
                            }
                        }
                    },
                    onBack = ::finish,
                    onNavigateToLogin = { finish() },
                    isLoading = isLoading,
                    errorMessage = errorMessage
                )
            }
        }
    }

    private fun registerUser(
        fullName: String,
        username: String,
        phone: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        when {
            fullName.isBlank() || username.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank() -> {
                onResult(false, "Preencha todos os campos!")
                return
            }

            password.length < 6 -> {
                onResult(false, "A senha deve ter pelo menos 6 caracteres.")
                return
            }
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    val userData = hashMapOf(
                        "fullName" to fullName,
                        "username" to username,
                        "phone" to phone,
                        "email" to email
                    )

                    if (userId != null) {
                        db.collection("users").document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                auth.signOut()
                                onResult(true, null)
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Erro ao salvar dados: ${e.message}")
                            }
                    } else {
                        onResult(false, "Erro ao obter ID do usuário.")
                    }
                } else {
                    onResult(false, "Erro ao cadastrar: ${task.exception?.message}")
                }
            }
    }
}