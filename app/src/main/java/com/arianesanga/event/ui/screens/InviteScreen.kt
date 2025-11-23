package com.arianesanga.event.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
fun InviteScreen(eventId: Int, navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val guestRepo = remember { GuestRepository(db.guestDao(), GuestRemoteRepository()) }
    val eventDao = db.eventDao()
    val auth = remember { AuthService() }

    var eventName by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    val coroutine = rememberCoroutineScope()

    LaunchedEffect(eventId) {
        withContext(Dispatchers.IO) {
            val ev = eventDao.getEventById(eventId)
            eventName = ev?.name
            loading = false
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))

                Text("Convite para Evento", color = Color(0xFF0F172A), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                if (loading) {
                    Text("Carregando...", color = Color.Gray)
                } else {
                    Text(eventName ?: "Evento não disponível localmente", color = Color.DarkGray)
                }

                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(modifier = Modifier.weight(1f), onClick = { navController.navigate("home") }) {
                        Text("Recusar")
                    }

                    Button(modifier = Modifier.weight(1f), onClick = {
                        coroutine.launch {
                            val user = auth.getCurrentUser()
                            if (user == null) {
                                Toast.makeText(context, "Faça login para aceitar o convite!", Toast.LENGTH_LONG).show()
                                navController.navigate("login")
                                return@launch
                            }

                            // cria convidado com referência ao usuário logado
                            val newGuest = Guest(eventId = eventId, name = user.email ?: "Usuário", email = user.email, firebaseUid = user.uid, status = "confirmed")
                            withContext(Dispatchers.IO) {
                                guestRepo.insertGuest(newGuest)
                            }

                            Toast.makeText(context, "Convite aceito! Você foi adicionado aos convidados.", Toast.LENGTH_LONG).show()
                            navController.navigate("event_guests/$eventId") {
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Text("Aceitar")
                    }
                }
            }
        }
    }
}
