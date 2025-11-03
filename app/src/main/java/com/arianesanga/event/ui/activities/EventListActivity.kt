package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.database.EventoDatabase
import com.arianesanga.event.data.repository.EventoRepository
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.viewmodels.EventoViewModel
import com.arianesanga.event.viewmodels.EventoViewModelFactory

class EventListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = EventoDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        val viewModel = ViewModelProvider(
            this,
            EventoViewModelFactory(repository)
        )[EventoViewModel::class.java]

        // Carrega os eventos
        viewModel.carregarEventos()
        viewModel.sincronizarEventosFirebase()

        setContent {
            EventTheme {
                EventListScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(viewModel: EventoViewModel) {
    val eventos by viewModel.eventos.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Eventos") })
        }
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
                            // Configuração para alinhar e espaçar os botões
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Container para os botões de texto (Convidados e Tarefas)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Botão para ver convidados
                                Button(onClick = {
                                    context.startActivity(Intent(context, ConvidadoActivity::class.java).apply {
                                        putExtra("eventoId", evento.id)
                                    })
                                }) {
                                    Text("Convidados")
                                }

                                // Botão para ver Tarefas
                                Button(onClick = {
                                    context.startActivity(Intent(context, TarefaActivity::class.java).apply {
                                        putExtra("eventoId", evento.id)
                                    })
                                }) {
                                    Text("Tarefas")
                                }
                            }

                            // Botão de excluir evento (AGORA CORRETAMENTE DENTRO DA ROW EXTERNA)
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