package com.arianesanga.event.ui.activities

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.arianesanga.event.data.local.database.EventDatabase
import com.arianesanga.event.data.local.model.User
import com.arianesanga.event.data.local.repository.LocalUserRepository
import com.arianesanga.event.data.remote.repository.RemoteUserRepository
import com.arianesanga.event.ui.screens.EditProfileScreen
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.utils.Image
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class EditProfileActivity : ComponentActivity() {

    private lateinit var localRepo: LocalUserRepository
    private val remoteRepo = RemoteUserRepository()
    private val auth = Firebase.auth

    private val imageUri = mutableStateOf<Uri?>(null)

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { imageUri.value = it }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Permissão negada para acessar fotos.", Toast.LENGTH_SHORT).show()
            } else {
                pickImageLauncher.launch("image/*")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        localRepo = LocalUserRepository(EventDatabase.getDatabase(applicationContext).userDao())

        val fullnameState = mutableStateOf("")
        val usernameState = mutableStateOf("")
        val phoneState = mutableStateOf("")
        val currentPasswordState = mutableStateOf("")
        val newPasswordState = mutableStateOf("")

        // Carregar dados do Room
        lifecycleScope.launch {
            auth.currentUser?.uid?.let { uid ->
                val user = localRepo.getUser(uid)
                fullnameState.value = user?.fullname ?: ""
                usernameState.value = user?.username ?: ""
                phoneState.value = user?.phone ?: ""
                imageUri.value = user?.photoUri?.let { Uri.parse(it) }
            }
        }

        setContent {
            EventTheme {
                EditProfileScreen(
                    selectedImageUri = imageUri.value,
                    onPickImage = { pickImage() },
                    onBack = { finish() },
                    fullnameState = fullnameState,
                    usernameState = usernameState,
                    phoneState = phoneState,
                    currentPasswordState = currentPasswordState,
                    newPasswordState = newPasswordState,
                    onSave = { fullname, username, phone, currentPassword, newPassword ->
                        saveProfile(fullname, username, phone, currentPassword, newPassword)
                    }
                )
            }
        }
    }

    private fun pickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission)
            == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun saveProfile(
        fullname: String,
        username: String,
        phone: String,
        currentPassword: String?,
        newPassword: String?
    ) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        lifecycleScope.launch {
            // Gera URI única para evitar caching
            val photoPath = imageUri.value?.let {
                Image.saveImageLocally(this@EditProfileActivity, it, "$uid-${System.currentTimeMillis()}")
            }

            val updatedUser = User(
                uid = uid,
                fullname = fullname,
                username = username,
                email = user.email ?: "",
                phone = phone,
                photoUri = photoPath
            )
            localRepo.insertUser(updatedUser)

            val data = mutableMapOf(
                "fullname" to fullname,
                "username" to username,
                "phone" to phone
            )
            photoPath?.let { data["photoUri"] = it }

            remoteRepo.updateUser(uid, data) { success ->
                if (success) Toast.makeText(this@EditProfileActivity, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
            }

            if (!currentPassword.isNullOrEmpty() && !newPassword.isNullOrEmpty()) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).addOnSuccessListener {
                    user.updatePassword(newPassword).addOnSuccessListener {
                        Toast.makeText(this@EditProfileActivity, "Senha alterada!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Retornar RESULT_OK para recarregar ProfileActivity
            setResult(RESULT_OK)
            finish()
        }
    }
}