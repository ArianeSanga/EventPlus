package com.arianesanga.event.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.EventRemoteRepository
import com.arianesanga.event.data.repository.EventRepository
import com.arianesanga.event.ui.components.AppState
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    navController: NavController,
    eventId: Int,
    appState: AppState
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { EventRepository(db.eventDao(), EventRemoteRepository()) }
    val auth = remember { AuthService() }

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    val coroutine = rememberCoroutineScope()

    var event by remember { mutableStateOf<Event?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()) }

    LaunchedEffect(eventId) {
        loading = true
        withContext(Dispatchers.IO) {
            val e = db.eventDao().getEventById(eventId)
            withContext(Dispatchers.Main) {
                event = e
                loading = false
            }
        }
    }

    var showWeatherSheet by remember { mutableStateOf(false) }

    var tasks by remember { mutableStateOf(emptyList<com.arianesanga.event.data.local.model.Task>()) }
    var percentUsed by remember { mutableStateOf(0.0) }

    LaunchedEffect(eventId) {
        withContext(Dispatchers.IO) {

            val ev = db.eventDao().getEventById(eventId)
            val taskList = db.taskDao().getTasksByEvent(eventId)

            // calcula tarefas concluídas / total
            val totalTasks = taskList.size
            val completedTasks = taskList.count { it.status == 2 }

            val pct = if (totalTasks > 0)
                completedTasks.toDouble() / totalTasks.toDouble()
            else 0.0

            withContext(Dispatchers.Main) {
                tasks = taskList
                event = ev
                percentUsed = pct
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
                title = "detalhes do evento",
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
                currentRoute = "event_list",
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
                            listOf(DARKBLUE, MEDIUMBLUE, MEDIUMBLUE, DARKBLUE, DARKBLUE)
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
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    event?.let { ev ->
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(20.dp))

                            val painterModel = remember(ev.imageUri) {
                                when {
                                    ev.imageUri.isNullOrBlank() -> null
                                    ev.imageUri.startsWith("content://") -> Uri.parse(ev.imageUri)
                                    ev.imageUri.startsWith("/") -> Uri.fromFile(File(ev.imageUri))
                                    ev.imageUri.startsWith("http") -> ev.imageUri
                                    else -> null
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF2F4F7)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (painterModel != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(painterModel),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Sem imagem", color = Color.Gray)
                                    }
                                }
                            }

                            Spacer(Modifier.height(5.dp))

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(ev.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Spacer(Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = DARKBLUE, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(ev.location, color = Color.DarkGray)
                                }

                                Spacer(Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = DARKBLUE, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(sdf.format(Date(ev.dateTime)), color = Color.DarkGray)
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column {
                                        Text("Descrição", fontSize = 14.sp, color = Color.Gray)
                                        Spacer(Modifier.height(8.dp))
                                        Text(ev.description.ifBlank { "Nenhuma descrição fornecida." }, color = Color(0xFF111827))
                                    }

                                    Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                                    Column {
                                        Text("Orçamento", color = Color.Gray, fontSize = 14.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {

                                            Text(
                                                "R$ ${"%.2f".format(ev.budget)}",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF111827)
                                            )

                                            val pctColor = budgetColor(percentUsed)

                                            Box(
                                                modifier = Modifier
                                                    .background(pctColor.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    "${(percentUsed * 100).toInt()}% usado",
                                                    color = pctColor,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Column {
                                            Text("Clima previsto", color = Color.Gray, fontSize = 14.sp)
                                            Spacer(Modifier.height(4.dp))

                                            if (ev.weatherIcon != null) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(
                                                            "https://openweathermap.org/img/wn/${ev.weatherIcon}.png"
                                                        ),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(42.dp)
                                                    )
                                                    Spacer(Modifier.width(2.dp))
                                                    Text(
                                                        "${"%.1f".format(ev.weatherTemp)}°C",
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DARKBLUE
                                                    )
                                                }
                                            } else {
                                                Text("Sem dados", color = Color.DarkGray, fontSize = 14.sp)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { showWeatherSheet = true },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DARKBLUE),
                                            border = BorderStroke(1.dp, DARKBLUE)
                                        ) {
                                            Text("Ver previsão", fontSize = 13.sp, color = DARKBLUE)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    OutlinedButton(
                                        onClick = { navController.navigate("edit_event/$eventId") },
                                        modifier = Modifier
                                            .height(45.dp)
                                            .width(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, MEDIUMBLUE),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = DARKBLUE
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = DARKBLUE,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { showDeleteConfirm = true },
                                        modifier = Modifier
                                            .height(45.dp)
                                            .width(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFFDC2626)
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Excluir",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { navController.navigate("tasks/$eventId") },
                                        modifier = Modifier
                                            .weight(0.8f)
                                            .height(45.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, DARKBLUE),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = DARKBLUE
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("Tarefas", fontSize = 13.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { navController.navigate("event_guests/$eventId") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(45.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, DARKBLUE),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = DARKBLUE
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("Convidados", fontSize = 13.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            OutlinedButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = DARKBLUE, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Voltar para lista", color = DARKBLUE, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    } ?: run {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Evento não encontrado.", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && event != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir evento") },
            text = { Text("Tem certeza que deseja excluir o evento \"${event!!.name}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    coroutine.launch {
                        withContext(Dispatchers.IO) {
                            repo.deleteEvent(event!!)
                        }
                        Toast.makeText(context, "Evento excluído", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }) {
                    Text("Excluir", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showWeatherSheet && event != null) {
        ModalBottomSheet(
            onDismissRequest = { showWeatherSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 35.dp, end = 35.dp, top = 5.dp, bottom = 35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "Previsão completa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                if (event!!.weatherIcon != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            "https://openweathermap.org/img/wn/${event!!.weatherIcon}@4x.png"
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(130.dp)
                    )
                }

                Text(
                    "${"%.1f".format(event!!.weatherTemp)}°C",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DARKBLUE
                )

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    WeatherInfoRow("Sensação térmica", "${"%.1f".format(event!!.weatherFeelsLike)}°C")
                    WeatherInfoRow("Umidade", "${event!!.weatherHumidity}%")
                    WeatherInfoRow("Vento", "${"%.1f".format(event!!.weatherWindSpeed)} m/s")
                    WeatherInfoRow("Condição", event!!.weatherDesc?.replaceFirstChar { it.uppercase() } ?: "—")
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun WeatherInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
        Text(value, color = Color(0xFF111827))
    }
}

fun budgetColor(percent: Double): Color {
    return when {
        percent >= 0.80 -> Color(0xFFDC2626)
        percent >= 0.50 -> Color(0xFFF59E0B)
        else -> Color(0xFF16A34A)
    }
}