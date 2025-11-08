package com.arianesanga.event.ui.screens

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arianesanga.event.R
import com.arianesanga.event.ui.activities.ProfileActivity
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.PINK

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    onBack: () -> Unit,
    onSave: (String, String, String, String?, String?) -> Unit,
    fullnameState: MutableState<String>,
    usernameState: MutableState<String>,
    phoneState: MutableState<String>,
    currentPasswordState: MutableState<String>,
    newPasswordState: MutableState<String>
) {
    val context = LocalContext.current

    // Controle de visibilidade das senhas
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Editar Perfil",
                showBackButton = true,
                onBack = onBack
            )
        },
        bottomBar = { BottomMenu(context, currentActivity = ProfileActivity::class.java) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 FOTO DE PERFIL
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.padding(top = 12.dp)
            ) {
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

                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .clip(CircleShape)
                        .clickable { onPickImage() },
                    color = PINK,
                    tonalElevation = 9.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Alterar foto",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 🔹 CARD DE INFORMAÇÕES PESSOAIS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Informações Pessoais",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DARKBLUE
                    )

                    Spacer(Modifier.height(16.dp))

                    // Nome completo
                    Text(
                        text = "Nome completo",
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = fullnameState.value,
                        onValueChange = { fullnameState.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 7.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFB0BEC5),
                            focusedBorderColor = Color(0xFFc2c2c2)
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Nome de usuário
                    Text(
                        text = "Nome de usuário",
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = usernameState.value,
                        onValueChange = { usernameState.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 7.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFB0BEC5),
                            focusedBorderColor = Color(0xFFc2c2c2)
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Telefone
                    Text(
                        text = "Telefone",
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = phoneState.value,
                        onValueChange = { phoneState.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 7.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFB0BEC5),
                            focusedBorderColor = Color(0xFFc2c2c2)
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // 🔹 CARD DE ALTERAÇÃO DE SENHA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Alterar Senha",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DARKBLUE
                    )

                    Spacer(Modifier.height(16.dp))

                    // Senha atual
                    Text(
                        text = "Senha atual",
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = currentPasswordState.value,
                        onValueChange = { currentPasswordState.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 7.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (showCurrentPassword)
                                Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                            IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (showCurrentPassword) "Ocultar senha" else "Mostrar senha",
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFB0BEC5),
                            focusedBorderColor = Color(0xFFc2c2c2)
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Nova senha
                    Text(
                        text = "Nova senha",
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = newPasswordState.value,
                        onValueChange = { newPasswordState.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 7.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (showNewPassword)
                                Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (showNewPassword) "Ocultar senha" else "Mostrar senha",
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFB0BEC5),
                            focusedBorderColor = Color(0xFFc2c2c2)
                        )
                    )
                }
            }

            Spacer(Modifier.height(25.dp))

            // 🔹 BOTÃO DE SALVAR
            Button(
                onClick = {
                    onSave(
                        fullnameState.value,
                        usernameState.value,
                        phoneState.value,
                        currentPasswordState.value.takeIf { it.isNotEmpty() },
                        newPasswordState.value.takeIf { it.isNotEmpty() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.8F)
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0XFF5ca35c))
            ) {
                Text("Salvar Alterações", fontSize = 18.sp, color = Color.White)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}