package com.arianesanga.event.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arianesanga.event.ui.theme.EventTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable//desenha na tela
fun HomeScreenStyled(
    onCreateEventClick: () -> Unit,//acoes que seram executadas quando o usuario clicar no botao
    onViewEventsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(//layout base no superior da tela
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
            item {
                EventCard("Criar Evento", Icons.Default.Add, onCreateEventClick)
            }
            item {
                EventCard("Ver Eventos", Icons.Default.List, onViewEventsClick)
            }
            item {
                EventCard("Perfil", Icons.Default.Person, onProfileClick)
            }
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeScreenStyled() {
    EventTheme {
        HomeScreenStyled(
            onCreateEventClick = {},
            onViewEventsClick = {},
            onProfileClick = {}
        )
    }
}