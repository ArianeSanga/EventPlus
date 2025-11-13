package com.arianesanga.event.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.EventRemoteRepository
import com.arianesanga.event.data.repository.EventRepository
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(navController: NavController) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Repositórios
    val db = remember { AppDatabase.getInstance(context) }
    val localDao = db.eventDao()
    val remoteRepo = remember { EventRemoteRepository() }
    val repository = remember { EventRepository(localDao, remoteRepo) }
    val auth = remember { AuthService() }

    // Estados
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Estado do DatePicker
    var showDatePicker by remember { mutableStateOf(false) }

    // Selecionar imagem (usando pointerInput para evitar problemas de Indication)
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "criar evento",
                showBackButton = true,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomMenu(
                currentRoute = "create_event",
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Imagem do evento (pointerInput em vez de clickable)
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCBD5E1))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            imagePicker.launch("image/*")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Imagem do evento",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Adicionar imagem", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Campos
            EventTextField(
                value = name,
                label = "Nome do Evento",
            ) { name = it }

            EventTextField(
                value = description,
                label = "Descrição",
            ) { description = it }

            EventTextField(
                value = location,
                label = "Local",
            ) { location = it }

            EventTextField(
                value = budget,
                label = "Orçamento (R$)",
                onValueChange = { input ->
                    budget = input.filter { it.isDigit() || it == '.' }
                }
            )

            // Campo de data personalizado (usa pointerInput para abrir DatePicker)
            EventDateField(
                date = date,
                onClick = { showDatePicker = true }
            )

            Spacer(Modifier.height(24.dp))

            // Botão de salvar
            Button(
                onClick = {
                    if (isSaving) return@Button

                    if (name.isBlank() || description.isBlank() || location.isBlank()
                        || budget.isBlank() || date.isBlank()) {

                        Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val userUid = auth.getCurrentUser()?.uid
                    if (userUid == null) {
                        Toast.makeText(context, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val event = Event(
                        userUid = userUid,
                        name = name,
                        description = description,
                        date = date,
                        location = location,
                        budget = budget.toDoubleOrNull() ?: 0.0,
                        imageUri = imageUri?.toString()
                    )

                    isSaving = true
                    // salva e após o sucesso navega para event_list
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            repository.insertEvent(event)
                            // Após inserir (local + remote), navegue para a listagem no Main
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show()
                                // navega e limpa a stack para evitar voltar pra tela de criação
                                navController.navigate("event_list") {
                                    launchSingleTop = true
                                    popUpTo("create_event") { inclusive = true }
                                }
                            }
                        } catch (e: Exception) {
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
            ) {
                if (isSaving)
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                else
                    Text("Salvar Evento", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selected ->
                date = selected
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun EventTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1E3A8A),
            focusedLabelColor = Color(0xFF1E3A8A)
        )
    )
}

@Composable
fun EventDateField(
    date: String,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = if (date.isEmpty()) "Selecionar data" else date,
        onValueChange = {},
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            // pointerInput detectTapGestures para abrir o picker (evita Indication issues)
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        label = { Text("Data do Evento") },
        leadingIcon = {
            Icon(
                Icons.Default.DateRange,
                contentDescription = "",
                tint = Color(0xFF1E3A8A)
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color(0xFF1E3A8A),
            disabledLabelColor = Color(0xFF1E3A8A),
            disabledLeadingIconColor = Color(0xFF1E3A8A)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val calendar = Calendar.getInstance().apply { timeInMillis = it }
                        val formatted = "%02d/%02d/%04d".format(
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR)
                        )
                        onDateSelected(formatted)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}