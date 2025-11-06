package com.arianesanga.event.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.arianesanga.event.ui.screens.EditProfileScreen
import com.arianesanga.event.ui.theme.EventTheme
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditProfileActivity : ComponentActivity() {

    private var imageUri = mutableStateOf<Uri?>(null)

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { imageUri.value = it }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) pickImageLauncher.launch("image/*")
            else Toast.makeText(this, "Permissão negada.", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fullNameState = mutableStateOf("")
        val usernameState = mutableStateOf("")
        val phoneState = mutableStateOf("")
        val currentPasswordState = mutableStateOf("")
        val newPasswordState = mutableStateOf("")

        // Carregar dados do usuário
        val user = Firebase.auth.currentUser
        val db = FirebaseFirestore.getInstance()
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        fullNameState.value = doc.getString("fullName") ?: ""
                        usernameState.value = doc.getString("username") ?: ""
                        phoneState.value = doc.getString("phone") ?: ""
                    }
                }
        }

        setContent {
            EventTheme {
                EditProfileScreen(
                    selectedImageUri = imageUri.value,
                    onPickImage = { checkPermissionAndPickImage() },
                    onBack = { finish() },
                    onSave = { fullName, username, phone, currentPassword, newPassword ->
                        saveProfile(fullName, username, phone, currentPassword, newPassword)
                    },
                    fullNameState = fullNameState,
                    usernameState = usernameState,
                    phoneState = phoneState,
                    currentPasswordState = currentPasswordState,
                    newPasswordState = newPasswordState
                )
            }
        }
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> pickImageLauncher.launch("image/*")
            shouldShowRequestPermissionRationale(permission) -> requestPermissionLauncher.launch(permission)
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun saveProfile(
        fullName: String,
        username: String,
        phone: String,
        currentPassword: String?,
        newPassword: String?
    ) {
        val user = Firebase.auth.currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance().reference
        val uid = user.uid
        val userRef = db.collection("users").document(uid)

        fun updateFirestore(photoUrl: String?) {
            val data = mapOf(
                "fullName" to fullName,
                "username" to username,
                "phone" to phone,
                "photoUrl" to (photoUrl ?: user.photoUrl?.toString())
            )
            userRef.update(data)
                .addOnSuccessListener { Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { Toast.makeText(this, "Erro ao atualizar perfil.", Toast.LENGTH_SHORT).show() }
        }

        // Upload imagem se existir
        if (imageUri.value != null) {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri.value!!)
                inputStream?.let {
                    val tempFile = File.createTempFile("profile_$uid", ".png", cacheDir)
                    val output = FileOutputStream(tempFile)
                    it.copyTo(output)
                    output.close()
                    it.close()

                    val imageRef = storage.child("profile_pictures/$uid.jpg")
                    imageRef.putFile(Uri.fromFile(tempFile))
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                updateFirestore(uri.toString())
                                tempFile.delete()
                            }
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            Toast.makeText(this, "Erro ao enviar imagem: ${e.message}", Toast.LENGTH_LONG).show()
                            tempFile.delete()
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Erro ao processar imagem: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            updateFirestore(null)
        }

        // Atualizar senha opcional
        if (!currentPassword.isNullOrEmpty() && !newPassword.isNullOrEmpty()) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnSuccessListener { user.updatePassword(newPassword)
                    .addOnSuccessListener { Toast.makeText(this, "Senha alterada!", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Erro ao alterar senha.", Toast.LENGTH_SHORT).show() }
                }
                .addOnFailureListener { Toast.makeText(this, "Senha atual incorreta.", Toast.LENGTH_SHORT).show() }
        }
    }
}