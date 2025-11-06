package com.arianesanga.event.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arianesanga.event.ui.theme.EventTheme
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance() // inicializa FirebaseAuth

        setContent {
            EventTheme {
                ForgotPasswordScreen(
                    onBack = ::finish,
                    onSendReset = ::sendPasswordResetEmail
                )
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "E-mail de redefinição enviado!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val error = task.exception?.message ?: "Erro ao enviar e-mail."
                    Toast.makeText(this, "Falha: $error", Toast.LENGTH_LONG).show()
                }
            }
    }
}