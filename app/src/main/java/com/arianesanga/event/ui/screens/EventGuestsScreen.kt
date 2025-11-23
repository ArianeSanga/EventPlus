package com.arianesanga.event.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.Guest
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.GuestRemoteRepository
import com.arianesanga.event.data.repository.GuestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventGuestsScreen(eventId: Int, navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val guestDao = db.guestDao()
    val guestRepo = remember { GuestRepository(guestDao, GuestRemoteRepository()) }

    var guests by remember { mutableStateOf<List<Guest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }

    val coroutine = rememberCoroutineScope()

    // Carregar convidados (local + remoto)
    LaunchedEffect(eventId) {
        loading = true
        withContext(Dispatchers.IO) {
            // LOCAL
            val localList = guestRepo.getGuestsByEvent(eventId)
            guests = localList
        }
        loading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Convidados", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))
                    )
                )
                .padding(padding)
        ) {

            Column(modifier = Modifier.padding(20.dp)) {

                // 🔴 Botão CONVIDAR + Botão ADICIONAR
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    ElevatedButton(
                        onClick = {
                            val link = "https://arianesanga.github.io/eventplus-download/?eventId=$eventId"
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Você foi convidado para um evento! Abra: $link")
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Compartilhar convite"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Convidar", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .width(56.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // LOADING
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                    return@Column
                }

                // NENHUM CONVIDADO
                if (guests.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum convidado ainda.", color = Color.White)
                    }
                }

                // LISTA DE CONVIDADOS
                else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(guests) { guest ->
                            GuestCard(
                                guest = guest,
                                onStatusChange = { status ->
                                    coroutine.launch {
                                        guestRepo.updateGuest(guest.copy(status = status))
                                        guests = guestRepo.getGuestsByEvent(eventId)
                                    }
                                },
                                onRemove = {
                                    coroutine.launch {
                                        guestRepo.deleteGuest(guest)
                                        guests = guestRepo.getGuestsByEvent(eventId)
                                        Toast.makeText(context, "${guest.name} removido", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // DIALOG PARA ADICIONAR CONVIDADO
    if (showAddDialog) {
        AddGuestDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, email, phone ->
                coroutine.launch {
                    withContext(Dispatchers.IO) {
                        guestRepo.insertGuest(
                            Guest(
                                eventId = eventId,
                                name = name,
                                email = email.ifBlank { null },
                                phone = phone.ifBlank { null }
                            )
                        )
                    }
                    guests = guestRepo.getGuestsByEvent(eventId)
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun AddGuestDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar convidado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefone") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onAdd(name, email, phone) }) {
                Text("Adicionar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun GuestCard(
    guest: Guest,
    onStatusChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor by animateColorAsState(
        when (guest.status) {
            "confirmed" -> Color(0xFF10B981)
            "refused" -> Color(0xFFEF4444)
            else -> Color(0xFF6B7280)
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = "https://ui-avatars.com/api/?name=${guest.name}",
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(guest.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                guest.email?.let { Text(it, color = Color.Gray, fontSize = 13.sp) }

                Spacer(Modifier.height(6.dp))

                Box(
                    Modifier
                        .background(statusColor.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        guest.status.replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, "", tint = Color.DarkGray)
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

                DropdownMenuItem(
                    text = { Text("Confirmado") },
                    onClick = { expanded = false; onStatusChange("confirmed") }
                )
                DropdownMenuItem(
                    text = { Text("Pendente") },
                    onClick = { expanded = false; onStatusChange("pending") }
                )
                DropdownMenuItem(
                    text = { Text("Recusado") },
                    onClick = { expanded = false; onStatusChange("refused") }
                )
                DropdownMenuItem(
                    text = { Text("Remover", color = Color(0xFFEF4444)) },
                    onClick = { expanded = false; onRemove() }
                )
            }
        }
    }
}
