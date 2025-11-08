package com.arianesanga.event.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import coil.compose.rememberAsyncImagePainter
import com.arianesanga.event.ui.activities.CreateEventActivity
import com.arianesanga.event.ui.activities.ProfileActivity
import java.util.*

@Composable
fun CreateEventScreen(
    onSave: (String, String, String, String, Double, String?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val calendar = Calendar.getInstance()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "criar evento",
                showBackButton = true,
                onBack = onBack
            )
        },
        bottomBar = { BottomMenu(context, currentActivity = CreateEventActivity::class.java) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Adicionar Foto", color = Color.White)
                }
            }

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Evento") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Local") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = budget, onValueChange = { budget = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Orçamento (R$)") }, modifier = Modifier.fillMaxWidth())

            Button(onClick = {
                DatePickerDialog(context, { _, y, m, d -> date = "$d/${m + 1}/$y" },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) { Text(if (date.isEmpty()) "Selecionar Data" else date) }

            var isSaving by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    if (isSaving) return@Button
                    isSaving = true

                    if (name.isNotBlank() && description.isNotBlank() && location.isNotBlank() &&
                        date.isNotBlank() && budget.isNotBlank()) {

                        onSave(name, description, date, location, budget.toDoubleOrNull() ?: 0.0, imageUri?.toString())

                    } else {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                        isSaving = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Salvar Evento", color = Color.White)
            }
        }
    }
}