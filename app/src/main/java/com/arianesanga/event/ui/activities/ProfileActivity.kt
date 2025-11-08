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
                ProfileScreen(
                    onLogout = { logout() },
                    onEditProfile = { openEditProfile() },
                    refreshKey = refreshProfile,
                    onBack = { finish() }
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