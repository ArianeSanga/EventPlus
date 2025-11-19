package com.arianesanga.event.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.ui.components.AppState
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    appState: AppState
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val eventDao = db.eventDao()

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    var nextEvent by remember { mutableStateOf<Event?>(null) }
    var countdown by remember { mutableStateOf("--") }

    LaunchedEffect(Unit) {
        val events = eventDao.getAllEvents()

        nextEvent = events
            .filter { it.dateTime > System.currentTimeMillis() }
            .minByOrNull { it.dateTime }
    }

    LaunchedEffect(nextEvent) {
        if (nextEvent != null) {
            while (true) {
                countdown = calculateCountdown(nextEvent!!.dateTime)
                delay(1000)
            }
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
                title = "inicio",
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
                currentRoute = "home",
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(DARKBLUE, MEDIUMBLUE, MEDIUMBLUE, DARKBLUE, DARKBLUE)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                    .background(Color(0xFFf0f0f0))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    NextEventBanner(
                        event = nextEvent,
                        countdown = countdown,
                        onCreateEvent = { navController.navigate("create_event") }
                    )
                }
            }
        }
    }
}

@Composable
fun NextEventBanner(
    event: Event?,
    countdown: String,
    onCreateEvent: (() -> Unit)? = null
) {
    if (event == null) {
        EmptyBanner(onCreateEvent)
        return
    }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = sdf.format(Date(event.dateTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0EA5E9),
                            Color(0xFF2563EB),
                            Color(0xFF1E3A8A),
                        )
                    )
                )
                .padding(20.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = "Próximo Evento",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = event.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Começa em:",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )

                Text(
                    text = countdown,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun EmptyBanner(onCreateEvent: (() -> Unit)?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = Color(0xFF93C5FD),
                modifier = Modifier.size(46.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Nenhum evento criado ainda",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Crie seu primeiro evento e acompanhe aqui!",
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            if (onCreateEvent != null) {
                Button(
                    onClick = onCreateEvent,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Criar Evento", color = Color.White)
                }
            }
        }
    }
}

/* ---------------------------------------------------------
    FUNÇÃO DE CONTAGEM REGRESSIVA (AGORA COM LONG)
--------------------------------------------------------- */
fun calculateCountdown(eventMillis: Long): String {
    val diff = eventMillis - System.currentTimeMillis()
    if (diff <= 0) return "Agora!"

    val days = diff / (1000 * 60 * 60 * 24)
    val hours = (diff / (1000 * 60 * 60)) % 24
    val minutes = (diff / (1000 * 60)) % 60
    val seconds = (diff / 1000) % 60

    return "%02dd %02dh %02dm %02ds".format(days, hours, minutes, seconds)
}