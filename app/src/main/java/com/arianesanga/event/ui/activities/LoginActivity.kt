package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.arianesanga.event.data.local.database.EventDatabase
import com.arianesanga.event.data.local.model.User
import com.arianesanga.event.data.local.repository.LocalUserRepository
import com.arianesanga.event.ui.screens.LoginScreen
import com.arianesanga.event.ui.theme.EventTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var localRepo: LocalUserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        localRepo = LocalUserRepository(EventDatabase.getDatabase(applicationContext).userDao())

        setContent {
            EventTheme {
                LoginScreen(
                    onLogin = { email, password, onFinish ->
                        loginUser(email, password, onFinish)
                    },
                    onBack = ::finish,
                    onForgotPassword = {
                        Toast.makeText(this, "Funcionalidade não implementada", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun loginUser(email: String, password: String, onFinish: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onFinish()
                if (task.isSuccessful) {
                    val uid = auth.currentUser!!.uid
                    val db = FirebaseFirestore.getInstance()
                    db.collection("user").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                lifecycleScope.launch {
                                    localRepo.insertUser(
                                        User(
                                            uid = uid,
                                            fullname = doc.getString("fullname") ?: "",
                                            username = doc.getString("username") ?: "",
                                            email = doc.getString("email") ?: "",
                                            phone = doc.getString("phone") ?: ""
                                        )
                                    )
                                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                    finishAffinity()
                                }
                            }
                        }
                } else {
                    Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_LONG).show()
                }
            }
    }
}