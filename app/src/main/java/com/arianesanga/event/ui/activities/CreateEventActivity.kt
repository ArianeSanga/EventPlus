package com.arianesanga.event.ui.activities

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberAsyncImagePainter
import com.arianesanga.event.data.database.EventoDatabase
import com.arianesanga.event.data.model.Evento
import com.arianesanga.event.data.repository.EventoRepository
import com.arianesanga.event.viewmodels.EventoViewModel
import com.arianesanga.event.viewmodels.EventoViewModelFactory
import java.util.*

class CreateEventActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = EventoDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        val factory = EventoViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[EventoViewModel::class.java]

        setContent {
            MaterialTheme {
                CreateEventScreen(viewModel)
            }
        }
    }
}

@Composable
fun CreateEventScreen(viewModel: EventoViewModel) {
    val context = LocalContext.current
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var data by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    var orcamento by remember { mutableStateOf("") }
    var imagemUri by remember { mutableStateOf<Uri?>(null) }

    val calendar = Calendar.getInstance()

    // Permissão para acessar fotos
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) Toast.makeText(context, "Permissão negada", Toast.LENGTH_SHORT).show()
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(permission)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagemUri = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Criar Novo Evento",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF2E7D32)
        )

        // Imagem redonda estilo perfil
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imagemUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imagemUri),
                    contentDescription = "Imagem do evento",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Adicionar Foto", color = Color.White)
            }
        }

        // Campos do evento
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome do Evento") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = local,
            onValueChange = { local = it },
            label = { Text("Local") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = orcamento,
            onValueChange = { orcamento = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Orçamento (R$)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Seletor de data
        Button(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        data = "$dayOfMonth/${month + 1}/$year"
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
        ) {
            Text(if (data.isEmpty()) "Selecionar Data" else data)
        }

        // Botão salvar evento
        Button(
            onClick = {
                when {
                    nome.isBlank() -> Toast.makeText(context, "Digite o nome do evento", Toast.LENGTH_SHORT).show()
                    descricao.isBlank() -> Toast.makeText(context, "Digite a descrição", Toast.LENGTH_SHORT).show()
                    local.isBlank() -> Toast.makeText(context, "Digite o local", Toast.LENGTH_SHORT).show()
                    data.isBlank() -> Toast.makeText(context, "Selecione a data", Toast.LENGTH_SHORT).show()
                    orcamento.isBlank() -> Toast.makeText(context, "Digite o orçamento", Toast.LENGTH_SHORT).show()
                    else -> {
                        val evento = Evento(
                            nome = nome,
                            descricao = descricao,
                            data = data,
                            local = local,
                            orcamento = orcamento.toDoubleOrNull() ?: 0.0,
                            fotoUri = imagemUri?.toString() ?: ""
                        )
                        viewModel.adicionarEvento(evento)
                        Toast.makeText(context, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show()

                        (context as? ComponentActivity)?.finish()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("Salvar Evento", color = Color.White)

        }
    }
}
