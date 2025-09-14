package com.arianesanga.event.views

import android.content.Intent
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
import com.arianesanga.event.HomeActivity
import com.arianesanga.event.ui.theme.EventTheme

class CreateEventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventTheme {
                CreateEventScreen()
            }
        }
    }
}

@Composable
fun CreateEventScreen() {

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventBudget by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventTime by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Launcher para escolher imagem
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Imagem redonda do evento
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

        Spacer(modifier = Modifier.height(8.dp))

        // Botão para escolher imagem
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Escolher Foto do Evento")
        }

        // Nome do evento
        TextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Nome do Evento") },
            modifier = Modifier.fillMaxWidth()
        )

        // Descrição
        TextField(
            value = eventDescription,
            onValueChange = { eventDescription = it },
            label = { Text("Descrição do Evento") },
            modifier = Modifier.fillMaxWidth()
        )

        // Orçamento
        TextField(
            value = eventBudget,
            onValueChange = { eventBudget = it },
            label = { Text("Orçamento (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Data
        TextField(
            value = eventDate,
            onValueChange = { eventDate = it },
            label = { Text("Data (dd/mm/aaaa)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Hora
        TextField(
            value = eventTime,
            onValueChange = { eventTime = it },
            label = { Text("Hora (hh:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Local
        TextField(
            value = eventLocation,
            onValueChange = { eventLocation = it },
            label = { Text("Local") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão criar evento
        Button(
            onClick = {
                // Mensagem de sucesso
                Toast.makeText(context, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show()

                // Voltar para a HomeActivity
                context.startActivity(Intent(context, HomeActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar Evento")
        }
    }
}
