package com.arianesanga.event.views

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.arianesanga.event.data.Evento
import com.arianesanga.event.data.EventoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenStyled(
    viewModel: EventoViewModel,
    onCreateEventClick: () -> Unit,
    onViewEventsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val eventos by viewModel.eventos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event+") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A2342),
                    titleContentColor = Color.White
                )
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botões principais (chamam callbacks passados pela Activity)
            item {
                EventCard("Criar Evento", Icons.Default.Add) {
                    onCreateEventClick()
                }
            }
            item {
                EventCard("Ver Eventos", Icons.Default.List) {
                    onViewEventsClick()
                }
            }
            item {
                EventCard("Perfil", Icons.Default.Person) {
                    onProfileClick()
                }
            }

            // Lista de eventos existentes
            items(eventos) { evento ->
                EventCardEvento(evento)
            }
        }
    }
}

@Composable
fun EventCardEvento(evento: Evento) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nome: ${evento.nome}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Descrição: ${evento.descricao}")
            Text(text = "Data: ${evento.data}")
            Text(text = "Local: ${evento.local}")
            Text(text = "Orçamento: R$ ${evento.orcamento}")
        }
    }
}

@Composable
fun EventCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF0A2342))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
