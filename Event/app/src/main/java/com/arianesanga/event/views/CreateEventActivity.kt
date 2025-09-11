package com.arianesanga.event.views

import android.inputmethodservice.Keyboard
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.test.espresso.base.Default
import com.arianesanga.event.ui.theme.EventTheme
import coil.compose.AsyncImage

class CreateEventActivity : ComponentActivity(){
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


    //laucher para selecionar a imagem
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri // aqui a imagem selecionada é salva
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        //botao para escolher imagem
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Escolher Foto do Evento")
        }

        //mostrar a imagem escolhida
        imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Foto do Evento",
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
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

        Button(
            onClick = {
                // Aqui você salva no Firestore ou no banco
                // ex: salvarEvento(eventName, eventDescription, eventBudget, eventDate, eventTime, eventLocation)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar Evento")
        }
    }
}

