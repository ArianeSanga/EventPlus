package com.arianesanga.event.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arianesanga.event.R
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val eventDao = db.eventDao()

    var nextEvent by remember { mutableStateOf<Event?>(null) }
    var countdown by remember { mutableStateOf("") }

    // 🔹 Buscar próximo evento
    LaunchedEffect(Unit) {
        val events = eventDao.getAllEvents()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val upcoming = events.mapNotNull { event ->
            try {
                val date = sdf.parse(event.date)
                if (date != null && date.after(Date())) event to date else null
            } catch (_: Exception) {
                null
            }
        }.minByOrNull { it.second }?.first

        nextEvent = upcoming
    }

    // 🔹 Atualizar cronômetro
    LaunchedEffect(nextEvent) {
        if (nextEvent != null) {
            while (true) {
                countdown = calculateCountdown(nextEvent!!.date)
                delay(1000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "início",
                showBackButton = false
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔥 Banner
            NextEventBanner(event = nextEvent, countdown = countdown)

            Spacer(Modifier.height(24.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo do App",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Bem-vindo(a) ao EventApp 🎉",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Gerencie seus eventos, acompanhe suas atividades e aproveite ao máximo sua experiência!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun NextEventBanner(event: Event?, countdown: String) {

    if (event == null) {
        EmptyBanner()
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0EA5E9), // azul claro
                            Color(0xFF2563EB), // azul médio
                            Color(0xFF1E3A8A), // azul escuro
                        )
                    )
                )
                .padding(20.dp)
        ) {

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {

                // Título + nome do evento
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
                }

                Spacer(modifier = Modifier.height(12.dp))

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
fun EmptyBanner(onCreateEvent: (() -> Unit)? = null) {

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

            // Ícone chamativo
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = Color(0xFF93C5FD),
                modifier = Modifier.size(46.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nenhum evento criado ainda",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crie seu primeiro evento e acompanhe tudo aqui!",
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão opcional dentro do banner
            if (onCreateEvent != null) {
                Button(
                    onClick = onCreateEvent,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Criar Evento", color = Color.White)
                }
            }
        }
    }
}
fun calculateCountdown(dateString: String): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val eventDate = sdf.parse(dateString) ?: return "--"

        val now = Calendar.getInstance().timeInMillis
        val diff = eventDate.time - now

        if (diff <= 0) return "Agora!"

        val days = diff / (1000 * 60 * 60 * 24)
        val hours = (diff / (1000 * 60 * 60)) % 24
        val minutes = (diff / (1000 * 60)) % 60
        val seconds = (diff / 1000) % 60

        "%02dd %02dh %02dm %02ds".format(days, hours, minutes, seconds)
    } catch (e: Exception) {
        "--"
    }
}

