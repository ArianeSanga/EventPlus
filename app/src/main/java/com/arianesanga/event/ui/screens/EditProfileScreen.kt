package com.arianesanga.event.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.arianesanga.event.R
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.User
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.UserRemoteRepository
import com.arianesanga.event.data.repository.UserRepository
import com.arianesanga.event.ui.components.AppState
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.BLACK
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    appState: AppState
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    val auth = remember { AuthService() }
    val userDao = remember { AppDatabase.getInstance(context).userDao() }
    val repo = remember { UserRepository(userDao, UserRemoteRepository()) }

    val firebaseUser = auth.getCurrentUser()
    val uid = firebaseUser?.uid ?: return

    var fullname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(firebaseUser.email ?: "") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var showCurrentPass by remember { mutableStateOf(false) }
    var showNewPass by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repo.getUser(uid)?.let {
            fullname = it.fullname
            username = it.username
            phone = it.phone
            photoUri = it.photoUri?.let { uri -> Uri.parse(uri) }
        }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {

                val savedPath = copyImageToInternalStorage(
                    context = context,
                    uri = uri,
                    filename = "profile_$uid.jpg"
                )

                if (savedPath.isNotEmpty()) {
                    photoUri = Uri.parse(savedPath)

                    scope.launch {
                        val updated = User(
                            uid = uid,
                            fullname = fullname,
                            username = username,
                            email = email,
                            phone = phone,
                            photoUri = savedPath
                        )

                        repo.updateUser(updated)
                    }

                    Toast.makeText(context, "Foto atualizada!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            val unread = appState.unreadCount.collectAsState(initial = 0).value

            val notifications by appState.notificationRepo
                .notificationsFlow()
                .collectAsState(initial = emptyList())
            TopAppBar(
                title = "editar perfil",
                showBackButton = true,
                onBack = { navController.popBackStack() },
                notificationCount = unread,
                notifications = notifications,
                onNotificationClick = {
                    appState.nav.navigate("notifications")
                },
                appState = appState
            )
        },
        bottomBar = {
            BottomMenu(
                currentRoute = "profile",
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
    ) { padding ->

        Box(Modifier.fillMaxSize()) {

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(DARKBLUE, MEDIUMBLUE, MEDIUMBLUE, DARKBLUE)
                        )
                    )
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                    .background(Color(0xFFF0F0F0))
            ) {

                Column(
                    Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(Modifier.height(25.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        pickerLauncher.launch("image/*")
                                    }
                                )
                            }
                            .size(180.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        AsyncImage(
                            model = photoUri?.toString() ?: R.drawable.account,
                            contentDescription = "Foto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Toque na foto para alterar",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(22.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            Modifier
                                .padding(18.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            Text(
                                "Informações pessoais",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BLACK
                            )

                            LabeledInputBase(
                                label = "Nome completo",
                                value = fullname,
                                placeholder = "Seu nome",
                                onValueChange = { fullname = it }
                            )

                            LabeledInputBase(
                                label = "Nome de usuário",
                                value = username,
                                placeholder = "Seu usuário",
                                onValueChange = { username = it }
                            )

                            LabeledInputBase(
                                label = "Telefone",
                                value = phone,
                                placeholder = "Ex: (16) 99999-0000",
                                onValueChange = { phone = it }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            Modifier
                                .padding(18.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            Text(
                                "Alterar senha",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BLACK
                            )

                            LabeledPasswordInput(
                                label = "Senha atual",
                                value = currentPass,
                                placeholder = "Digite sua senha atual",
                                visible = showCurrentPass,
                                onToggle = { showCurrentPass = !showCurrentPass },
                                onValueChange = { currentPass = it }
                            )

                            LabeledPasswordInput(
                                label = "Nova senha",
                                value = newPass,
                                placeholder = "Digite uma nova senha",
                                visible = showNewPass,
                                onToggle = { showNewPass = !showNewPass },
                                onValueChange = { newPass = it }
                            )
                        }
                    }

                    Spacer(Modifier.height(26.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val updated = User(
                                    uid = uid,
                                    fullname = fullname,
                                    username = username,
                                    email = email,
                                    phone = phone,
                                    photoUri = photoUri?.toString()
                                )

                                repo.updateUser(updated)

                                Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MEDIUMBLUE,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "salvar alterações",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(115.dp))
                }
            }
        }
    }
}

@Composable
fun LabeledPasswordInput(
    label: String,
    value: String,
    placeholder: String,
    visible: Boolean,
    onToggle: () -> Unit,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DARKBLUE
        )
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            visualTransformation =
                if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MEDIUMBLUE
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MEDIUMBLUE,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MEDIUMBLUE
            )
        )
    }
}

fun uriToBase64(context: android.content.Context, uri: Uri): String {
    val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val bytes = outputStream.toByteArray()

    return Base64.getEncoder().encodeToString(bytes)
}

fun copyImageToInternalStorage(context: android.content.Context, uri: Uri, filename: String): String {
    val input = context.contentResolver.openInputStream(uri) ?: return ""
    val file = java.io.File(context.filesDir, filename)

    val output = file.outputStream()
    input.copyTo(output)

    input.close()
    output.close()

    return file.absolutePath
}