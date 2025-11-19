package com.arianesanga.event.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.arianesanga.event.R
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.UserRemoteRepository
import com.arianesanga.event.data.repository.UserRepository
import com.arianesanga.event.ui.components.AppState
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    appState: AppState
) {

    val context = LocalContext.current

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    val authService = remember { AuthService() }
    val userRemoteRepo = remember { UserRemoteRepository() }
    val userDao = remember { AppDatabase.getInstance(context).userDao() }
    val userRepo = remember { UserRepository(userDao, userRemoteRepo) }

    val firebaseUser = authService.getCurrentUser()
    val uid = firebaseUser?.uid ?: return

    var fullname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(firebaseUser.email ?: "") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val local = userRepo.getUser(uid)
        if (local != null) {
            fullname = local.fullname
            username = local.username
            phone = local.phone
            photoUri = local.photoUri
        }

        userRepo.fetchRemoteUser(uid)?.let {
            fullname = it.fullname
            username = it.username
            phone = it.phone
            photoUri = it.photoUri
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            val unread = appState.unreadCount.collectAsState(initial = 0).value

            val notifications by appState.notificationRepo
                .notificationsFlow()
                .collectAsState(initial = emptyList())
            TopAppBar(
                title = "meu perfil",
                showBackButton = false,
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

        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(DARKBLUE, MEDIUMBLUE, MEDIUMBLUE, DARKBLUE)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                    .background(Color(0xFFF0F0F0))
            ) {

                Column(
                    modifier = Modifier
                        .padding(26.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(Modifier.height(20.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(180.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        AsyncImage(
                            model = photoUri ?: R.drawable.account,
                            contentDescription = "Foto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(username, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(email, fontSize = 16.sp, color = Color.Gray)

                    Spacer(Modifier.height(15.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        colors = CardDefaults.cardColors(Color.White)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            InfoField("Nome", fullname)
                            InfoField("Usuário", username)
                            InfoField("Telefone", phone)
                        }
                    }

                    Spacer(Modifier.height(15.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        OutlinedButton(
                            onClick = { navController.navigate("edit_profile") },
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = MEDIUMBLUE
                            ),
                            border = BorderStroke(1.dp, MEDIUMBLUE)
                        ) {
                            Text("editar", fontSize = 18.sp, color = MEDIUMBLUE)
                        }

                        OutlinedButton(
                            onClick = {
                                authService.logout()
                                navController.navigate("main") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            border = BorderStroke(1.dp, Color.Black)
                        ) {
                            Text("sair", fontSize = 18.sp, color = Color.Black)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { /* deletar */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.Red),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Red
                        )
                    ) {
                        Text("excluir conta", fontSize = 18.sp)
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoField(label: String, value: String) {
    Text(label, color = MEDIUMBLUE, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Text(value, fontSize = 18.sp, color = Color.DarkGray)
    Spacer(Modifier.height(5.dp))
}