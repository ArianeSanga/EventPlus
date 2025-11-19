package com.arianesanga.event.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.UserRemoteRepository
import com.arianesanga.event.data.repository.UserRepository
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import com.arianesanga.event.ui.theme.PINK
import com.arianesanga.event.ui.theme.WHITE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BigPasswordTransformation(
    private val maskChar: Char = '●',
    private val size: Int = 20
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val transformed = AnnotatedString(
            text = maskChar.toString().repeat(text.length),
            spanStyles = listOf(
                AnnotatedString.Range(
                    SpanStyle(fontSize = size.sp),
                    start = 0,
                    end = text.length
                )
            )
        )

        return TransformedText(
            text = transformed,
            offsetMapping = OffsetMapping.Identity
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {

    val context = LocalContext.current
    val activity = context as ComponentActivity
    val lifecycleScope = activity.lifecycleScope

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
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = WHITE)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(DARKBLUE, MEDIUMBLUE, MEDIUMBLUE, DARKBLUE, DARKBLUE)
                    )
                )
                .padding(paddingValues)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "cadastre-se",
                    color = WHITE,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(25.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {

                    SimpleField(
                        value = fullname,
                        onChange = { fullname = it },
                        label = "nome completo"
                    )

                    SimpleField(
                        value = username,
                        onChange = { username = it },
                        label = "usuário"
                    )

                    SimpleField(
                        value = phone,
                        onChange = { phone = it },
                        label = "telefone",
                        keyboardType = KeyboardType.Phone
                    )

                    SimpleField(
                        value = email,
                        onChange = { email = it },
                        label = "e-mail",
                        keyboardType = KeyboardType.Email
                    )

                    PasswordSimpleField(
                        value = password,
                        onChange = { password = it },
                        label = "senha",
                        visible = passwordVisible,
                        onToggleVisibility = { passwordVisible = !passwordVisible }
                    )

                    PasswordSimpleField(
                        value = confirmPassword,
                        onChange = { confirmPassword = it },
                        label = "confirmar senha",
                        visible = confirmPasswordVisible,
                        onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))

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
                            email,
                            password,
                            onSuccess = { uid ->

                                val user = com.arianesanga.event.data.local.model.User(
                                    uid = uid,
                                    fullname = fullname,
                                    username = username,
                                    phone = phone,
                                    email = email
                                )

                                lifecycleScope.launch {
                                    withContext(Dispatchers.IO) {
                                        userRepo.insertUser(user)
                                    }

                                    isLoading = false

                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            },
                            onError = { message ->
                                isLoading = false
                                errorMessage = message
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PINK)
                ) {
                    if (isLoading)
                        CircularProgressIndicator(color = WHITE)
                    else
                        Text("criar uma conta", color = WHITE, fontSize = 18.sp)
                }

                Spacer(Modifier.height(18.dp))

                Row {
                    Text("possui cadastro? ", color = WHITE)
                    Text(
                        "faça login",
                        color = PINK,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate("login")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(1.dp))

        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = WHITE,
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .height(27.dp)
                .padding(vertical = 1.dp)
        )
    }
}


@Composable
fun PasswordSimpleField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 20.dp, vertical = 7.dp)
    ) {

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(2.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = WHITE,
                    fontSize = 14.sp
                ),
                visualTransformation = if (visible) VisualTransformation.None
                else BigPasswordTransformation(maskChar = '●', size = 15),
                modifier = Modifier
                    .weight(1f)
                    .height(27.dp)
                    .padding(vertical = 1.dp)
            )

            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier
                    .size(22.dp)
                    .offset(y = (-10).dp)
            ) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = PINK,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}