package com.arianesanga.event.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.*
import com.arianesanga.event.ui.theme.EventTheme

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
                    }
                }
            }
        }
    }

}
