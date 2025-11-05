package com.arianesanga.event.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arianesanga.event.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    onBack: () -> Unit,
    onSave: (String, String, String, String?, String?) -> Unit,
    fullNameState: MutableState<String>,
    usernameState: MutableState<String>,
    phoneState: MutableState<String>,
    currentPasswordState: MutableState<String>,
    newPasswordState: MutableState<String>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = selectedImageUri ?: R.drawable.account,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = onPickImage,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Alterar foto")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Campos editáveis
            OutlinedTextField(
                value = fullNameState.value,
                onValueChange = { fullNameState.value = it },
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = usernameState.value,
                onValueChange = { usernameState.value = it },
                label = { Text("Nome de usuário") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phoneState.value,
                onValueChange = { phoneState.value = it },
                label = { Text("Telefone") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = currentPasswordState.value,
                onValueChange = { currentPasswordState.value = it },
                label = { Text("Senha atual (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPasswordState.value,
                onValueChange = { newPasswordState.value = it },
                label = { Text("Nova senha (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    onSave(
                        fullNameState.value,
                        usernameState.value,
                        phoneState.value,
                        currentPasswordState.value.takeIf { it.isNotEmpty() },
                        newPasswordState.value.takeIf { it.isNotEmpty() }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
        }
    }
}