package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.arianesanga.event.data.local.database.EventDatabase
import com.arianesanga.event.data.local.model.User
import com.arianesanga.event.data.local.repository.LocalUserRepository
import com.arianesanga.event.data.remote.repository.RemoteUserRepository
import com.arianesanga.event.ui.screens.RegisterScreen
import com.arianesanga.event.ui.theme.EventTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var localRepo: LocalUserRepository
    private val remoteRepo = RemoteUserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        localRepo = LocalUserRepository(EventDatabase.getDatabase(applicationContext).userDao())

        setContent {
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            EventTheme {
                RegisterScreen(
                    onRegisterClick = { fullname, username, phone, email, password ->
                        if (fullname.isBlank() || username.isBlank() || phone.isBlank() ||
                            email.isBlank() || password.isBlank()
                        ) {
                            errorMessage = "Preencha todos os campos"
                            return@RegisterScreen
                        }

                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val uid = auth.currentUser!!.uid
                                    val userData = mapOf(
                                        "fullname" to fullname,
                                        "username" to username,
                                        "email" to email,
                                        "phone" to phone
                                    )

                                    remoteRepo.createUser(uid, userData) { success, _ ->
                                        if (success) {
                                            lifecycleScope.launch {
                                                localRepo.insertUser(
                                                    User(uid, fullname, username, email, phone)
                                                )
                                                auth.signOut()
                                                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                                finish()
                                            }
                                        }
                                    }
                                } else {
                                    errorMessage = task.exception?.message
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
}