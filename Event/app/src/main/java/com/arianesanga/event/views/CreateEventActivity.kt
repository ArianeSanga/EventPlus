package com.arianesanga.event.views

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arianesanga.event.data.Evento
import com.arianesanga.event.data.EventoDatabase
import com.arianesanga.event.data.EventoRepository
import com.arianesanga.event.data.EventoViewModel
import com.arianesanga.event.data.EventoViewModelFactory
import com.arianesanga.event.ui.theme.EventTheme

class CreateEventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cria Repository e ViewModel
        val database = EventoDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        val viewModel = EventoViewModelFactory(repository)
            .create(EventoViewModel::class.java)

        setContent {
            EventTheme {
                CreateEventScreen(viewModel)
            }
        }
    }
}

@Composable
fun CreateEventScreen(viewModel: EventoViewModel) {

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventBudget by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventTime by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Imagem redonda
        imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Foto do Evento",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Escolher Foto do Evento")
        }

        TextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Nome do Evento") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = eventDescription,
            onValueChange = { eventDescription = it },
            label = { Text("Descrição do Evento") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = eventBudget,
            onValueChange = { eventBudget = it },
            label = { Text("Orçamento (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = eventDate,
            onValueChange = { eventDate = it },
            label = { Text("Data (dd/mm/aaaa)") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = eventTime,
            onValueChange = { eventTime = it },
            label = { Text("Hora (hh:mm)") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = eventLocation,
            onValueChange = { eventLocation = it },
            label = { Text("Local") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Cria objeto Evento
                val evento = Evento(
                    nome = eventName,
                    descricao = eventDescription,
                    data = "Data: $eventDate\nHora: $eventTime",
                    local = eventLocation,
                    orcamento = eventBudget.toDoubleOrNull() ?: 0.0
                )

                // Adiciona no banco via ViewModel
                viewModel.adicionarEvento(evento)

                // Feedback
                Toast.makeText(context, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show()

                // Volta para a HomeActivity
                (context as? ComponentActivity)?.finish()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar Evento")
        }
    }
}
