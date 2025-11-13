package com.arianesanga.event.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.User
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.UserRemoteRepository
import com.arianesanga.event.data.repository.UserRepository
import com.arianesanga.event.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {

    val context = LocalContext.current
    val activity = context as ComponentActivity
    val lifecycleScope = activity.lifecycleScope
    val scope = rememberCoroutineScope()

    val appDb = remember { AppDatabase.getInstance(context) }
    val userRepo = remember { UserRepository(appDb.userDao(), UserRemoteRepository()) }
    val authService = remember { AuthService() }

    var fullname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = WHITE)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(DARKBLUE, MEDIUMBLUE, DARKBLUE)))
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("CRIAR CONTA", color = WHITE, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = fullname,
                onValueChange = { fullname = it },
                label = { Text("Nome completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = LIGHTBLUE,
                    focusedLabelColor = LIGHTBLUE,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedLeadingIconColor = LIGHTBLUE,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = LIGHTBLUE,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuário") },
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = LIGHTBLUE,
                    focusedLabelColor = LIGHTBLUE,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedLeadingIconColor = LIGHTBLUE,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = LIGHTBLUE,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefone") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = LIGHTBLUE,
                    focusedLabelColor = LIGHTBLUE,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedLeadingIconColor = LIGHTBLUE,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = LIGHTBLUE,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = LIGHTBLUE,
                    focusedLabelColor = LIGHTBLUE,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedLeadingIconColor = LIGHTBLUE,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = LIGHTBLUE,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = LIGHTBLUE,
                    focusedLabelColor = LIGHTBLUE,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedLeadingIconColor = LIGHTBLUE,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = LIGHTBLUE,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LIGHTBLUE,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = LIGHTBLUE,
                    focusedLabelColor = LIGHTBLUE,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedLeadingIconColor = LIGHTBLUE,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = LIGHTBLUE,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = WHITE,
                    unfocusedTextColor = WHITE,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage!!, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (fullname.isBlank() || username.isBlank() || phone.isBlank() ||
                        email.isBlank() || password.isBlank()
                    ) {
                        errorMessage = "Preencha todos os campos."
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMessage = "As senhas não coincidem."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    authService.registerUser(
                        email = email,
                        password = password,
                        onSuccess = { uid ->
                            lifecycleScope.launch {
                                try {
                                    userRepo.insertUser(
                                        User(
                                            uid = uid,
                                            fullname = fullname,
                                            username = username,
                                            email = email,
                                            phone = phone
                                        )
                                    )

                                    authService.logout()

                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }

                                } catch (e: Exception) {
                                    errorMessage = "Erro ao salvar usuário localmente."
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        onError = {
                            isLoading = false
                            errorMessage = it
                        }
                    )
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LIGHTBLUE),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                if (isLoading)
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WHITE)
                else
                    Text("CADASTRAR", color = WHITE)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text("Já tem uma conta? ", color = WHITE)
                Text(
                    "Faça Login",
                    color = LIGHTBLUE,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        navController.navigate("login")
                    }
                )
            }
        }
    }
}