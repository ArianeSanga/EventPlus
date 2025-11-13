package com.arianesanga.event.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(navController: NavController) {

    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val eventDao = db.eventDao()
    val scope = rememberCoroutineScope()

    var events by remember { mutableStateOf(emptyList<Event>()) }
    var isDeleting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            events = eventDao.getAllEvents()
        }
    }

    fun reload() {
        scope.launch(Dispatchers.IO) {
            events = eventDao.getAllEvents()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "meus eventos",
                showBackButton = false
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
    ) { paddingValues ->

        Column(
            Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            if (events.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhum evento criado ainda.",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(events, key = { it.id }) { event ->

                        EventListItem(
                            event = event,
                            onEdit = { id ->
                                navController.navigate("edit_event/$id")
                            },
                            onDelete = { id ->
                                isDeleting = true
                                scope.launch(Dispatchers.IO) {
                                    eventDao.deleteById(id)
                                    launch(Dispatchers.Main) {
                                        isDeleting = false
                                        reload()
                                    }
                                }
                            },
                            onTasks = { id ->
                                navController.navigate("event_tasks/$id")
                            },
                            onGuests = { id ->
                                navController.navigate("event_guests/$id")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventListItem(
    event: Event,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onTasks: (Int) -> Unit,
    onGuests: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1724)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Imagem do evento (se houver)
            if (!event.imageUri.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(event.imageUri)),
                    contentDescription = "${event.name} image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
            } else {
                // bloco visual mais limpo quando não há imagem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F1724))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Nome + local/data/orçamento
            Text(
                text = event.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF60A5FA))
                Text(event.location.ifBlank { "—" }, color = Color.White)
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF60A5FA))
                Text(event.date.ifBlank { "—" }, color = Color.White)
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Money, contentDescription = null, tint = Color(0xFF60A5FA))
                Text("R$ ${"%.2f".format(event.budget)}", color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            // Ações: editar / excluir / tarefas / convidados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: small group of action icons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { onEdit(event.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF93C5FD))
                    }
                    IconButton(onClick = { onDelete(event.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFF87171))
                    }
                }

                // Right side: tasks + guests
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onTasks(event.id) }) {
                        Icon(Icons.Default.List, contentDescription = "Tarefas", tint = Color(0xFF60A5FA))
                        Spacer(Modifier.width(6.dp))
                        Text("Tarefas", color = Color.White)
                    }
                    TextButton(onClick = { onGuests(event.id) }) {
                        Icon(Icons.Default.Group, contentDescription = "Convidados", tint = Color(0xFF60A5FA))
                        Spacer(Modifier.width(6.dp))
                        Text("Convidados", color = Color.White)
                    }
                }
            }
        }
    }
}