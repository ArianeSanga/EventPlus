package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arianesanga.event.data.local.database.EventDatabase
import com.arianesanga.event.data.local.repository.LocalUserRepository
import com.arianesanga.event.ui.screens.ProfileScreen
import com.arianesanga.event.ui.theme.EventTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.arianesanga.event.ui.activities.LoginActivity

class ProfileActivity : ComponentActivity() {

    private lateinit var localRepo: LocalUserRepository
    private val auth = Firebase.auth

    // Variável para forçar recomposição do Compose
    private var refreshProfile by mutableStateOf(0)

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshProfile++ // força reload do ProfileScreen
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        localRepo = LocalUserRepository(EventDatabase.getDatabase(applicationContext).userDao())
        loadProfileScreen()
    }

    private fun loadProfileScreen() {
        setContent {
            EventTheme {
                // ** A CHAMA DEVE SER AQUI, INCLUINDO TODOS OS PARÂMETROS **
                ProfileScreen(
                    onLogout = {
                        // 1. Faz o logout no Firebase
                        auth.signOut() // Use 'auth' que já está declarado

                        // 2. Inicia a Activity de Login e limpa a pilha
                        val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        // 3. Encerra a ProfileActivity para que não fique na pilha (evita a tela branca)
                        finish()
                    },
                    // Adicione os outros parâmetros que o ProfileScreen espera:
                    onEditProfile = ::openEditProfile,
                    refreshKey = refreshProfile,
                    onBack = ::finish // Usa o finish() da Activity para voltar
                )
            }
        }
    }

    private fun openEditProfile() {
        editProfileLauncher.launch(Intent(this, EditProfileActivity::class.java))
    }

    private fun logout() {
        auth.signOut()
        finish()
    }
}