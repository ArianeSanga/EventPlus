package com.arianesanga.event.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.arianesanga.event.R
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.User
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.UserRemoteRepository
import com.arianesanga.event.data.repository.UserRepository
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.PINK
import com.arianesanga.event.ui.theme.DARKBLUE
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Instâncias
    val authService = remember { AuthService() }
    val userRemoteRepo = remember { UserRemoteRepository() }
    val userDao = remember { AppDatabase.getInstance(context).userDao() }
    val userRepo = remember { UserRepository(userDao, userRemoteRepo) }

    val firebaseUser = authService.getCurrentUser()
    val uid = firebaseUser?.uid ?: run {
        // se não autenticado, volta para main
        navController.navigate("main") { popUpTo("home") { inclusive = true } }
        return
    }

    var fullname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(firebaseUser.email ?: "") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }

    // carregar dados locais/remote
    LaunchedEffect(uid) {
        userRepo.getUser(uid)?.let { u ->
            fullname = u.fullname
            username = u.username
            phone = u.phone
            photoUri = u.photoUri?.let { Uri.parse(it) }
        }
        userRepo.fetchRemoteUser(uid)?.let { u ->
            fullname = u.fullname
            username = u.username
            phone = u.phone
            photoUri = u.photoUri?.let { Uri.parse(it) }
        }
    }

    // image picker simplified (reuse your util)
    var selectedImageUri by remember { mutableStateOf(photoUri) }
    // pick image logic omitted; keep your Image.saveImageLocally util as before

    Scaffold(
        topBar = {
            TopAppBar(
                title = "editar perfil",
                showBackButton = true,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FOTO DE PERFIL
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(top = 12.dp)) {
                AsyncImage(
                    model = selectedImageUri ?: R.drawable.account,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )

                // botao trocar foto (implemente picker conforme sua util)
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .clip(CircleShape)
                        .clickable {
                            // lance picker — aqui você pode reutilizar o código do EditProfile antigo
                        },
                    color = PINK,
                    tonalElevation = 9.dp
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CameraAlt,
                        contentDescription = "Alterar foto",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Campos (reutilize InfoCard/PasswordCard do seu arquivo anterior)
            // Vou deixar campos simples aqui para manter o foco na integração com NavController

            OutlinedTextField(
                value = fullname,
                onValueChange = { fullname = it },
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nome de usuário") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Senha atual") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            imageVector = if (showCurrentPassword) androidx.compose.material.icons.Icons.Default.VisibilityOff else androidx.compose.material.icons.Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nova senha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            imageVector = if (showNewPassword) androidx.compose.material.icons.Icons.Default.VisibilityOff else androidx.compose.material.icons.Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        // salvar localmente
                        val updated = User(
                            uid = uid,
                            fullname = fullname,
                            username = username,
                            email = email,
                            phone = phone,
                            photoUri = selectedImageUri?.toString()
                        )
                        userRepo.updateUser(updated)

                        // atualizar senha se necessário
                        if (currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
                            firebaseUser.reauthenticate(credential)
                                .addOnSuccessListener {
                                    firebaseUser.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Senha alterada!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        }

                        Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // volta ao Profile
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f).height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DARKBLUE)
            ) {
                Text("Salvar Alterações", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    show: Boolean,
    onToggle: () -> Unit,
    onValueChange: (String) -> Unit
) {
    Text(
        text = label,
        fontSize = 15.sp,
        color = Color.DarkGray,
        fontWeight = FontWeight.SemiBold
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (show) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
            IconButton(onClick = onToggle) {
                Icon(icon, contentDescription = null, tint = Color.Gray)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFB0BEC5),
            focusedBorderColor = Color(0xFFc2c2c2)
        )
    )
}