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
import com.arianesanga.event.ui.activities.ConvidadoActivity
import com.arianesanga.event.ui.activities.EventListActivity
import com.arianesanga.event.ui.activities.TarefaActivity
import com.arianesanga.event.viewmodels.EventoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventoViewModel,
    onBack: () -> Unit
) {
    val eventos by viewModel.eventos.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Meus eventos",
                showBackButton = true,
                onBack = onBack
            )
        },
        bottomBar = { BottomMenu(context, currentActivity = EventListActivity::class.java) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(eventos) { evento ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nome: ${evento.nome}", style = MaterialTheme.typography.titleMedium)
                        Text("Descrição: ${evento.descricao}")
                        Text("Data e Hora: ${evento.data}")
                        Text("Local: ${evento.local}")
                        Text("Orçamento: R$${evento.orcamento}")

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
                                            putExtra("eventoId", evento.id)
                                        }
                                    )
                                }) {
                                    Text("Convidados")
                                }

                                Button(onClick = {
                                    context.startActivity(
                                        Intent(context, TarefaActivity::class.java).apply {
                                            putExtra("eventoId", evento.id)
                                        }
                                    )
                                }) {
                                    Text("Tarefas")
                                }
                            }

                            IconButton(onClick = { viewModel.deletarEvento(evento) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir evento"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}