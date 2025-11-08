package com.arianesanga.event.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.data.local.repository.LocalEventRepository
import com.arianesanga.event.data.remote.repository.RemoteEventRepository
import com.arianesanga.event.ui.activities.ConvidadoActivity
import com.arianesanga.event.ui.activities.CreateEventActivity
import com.arianesanga.event.ui.activities.EventListActivity
import com.arianesanga.event.ui.activities.TarefaActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    userUid: String?,
    localRepo: LocalEventRepository,
    remoteRepo: RemoteEventRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var eventos by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (userUid != null) {
            eventos = localRepo.getEventsByUser(userUid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Eventos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Delete, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = { BottomMenu(context, currentActivity = EventListActivity::class.java) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.startActivity(Intent(context, CreateEventActivity::class.java))
            }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        if (eventos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum evento cadastrado ainda.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(eventos, key = { it.id }) { evento ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nome: ${evento.name}", style = MaterialTheme.typography.titleMedium)
                            Text("Descrição: ${evento.description}")
                            Text("Data: ${evento.date}")
                            Text("Local: ${evento.location}")
                            Text("Orçamento: R$${evento.budget}")

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        context.startActivity(
                                            Intent(context, ConvidadoActivity::class.java).apply {
                                                putExtra("id", evento.id)
                                            }
                                        )
                                    }) { Text("Convidados") }

                                    Button(onClick = {
                                        context.startActivity(
                                            Intent(context, TarefaActivity::class.java).apply {
                                                putExtra("id", evento.id)
                                            }
                                        )
                                    }) { Text("Tarefas") }
                                }

                                IconButton(onClick = {
                                    scope.launch {
                                        localRepo.deleteEvent(evento)
                                        userUid?.let {
                                            remoteRepo.deleteEvent(it, evento.id) { }
                                        }
                                        eventos = localRepo.getEventsByUser(userUid!!)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir evento")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}