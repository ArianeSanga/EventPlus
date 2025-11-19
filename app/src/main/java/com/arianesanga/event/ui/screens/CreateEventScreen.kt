package com.arianesanga.event.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.arianesanga.event.api.ForecastItem
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.data.local.model.NotificationEntity
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.data.remote.repository.EventRemoteRepository
import com.arianesanga.event.data.remote.repository.NotificationRemoteRepository
import com.arianesanga.event.data.remote.repository.WeatherRepository
import com.arianesanga.event.data.repository.EventRepository
import com.arianesanga.event.data.repository.NotificationRepository
import com.arianesanga.event.notifications.NotificationHelper
import com.arianesanga.event.ui.components.AppState
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import com.arianesanga.event.utils.ApiKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    appState: AppState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    val db = remember { AppDatabase.getInstance(context) }
    val repository = remember { EventRepository(db.eventDao(), EventRemoteRepository()) }
    val auth = remember { AuthService() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var budgetMasked by remember { mutableStateOf("") }
    var rawBudget by remember { mutableStateOf("") }

    var pickedDateMillis by remember { mutableStateOf<Long?>(null) }
    var pickedHour by remember { mutableStateOf<Int?>(null) }
    var pickedMinute by remember { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, "Permissão negada.", Toast.LENGTH_SHORT).show()
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) imageUri = uri }

    fun pickImage() {
        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        pickImageLauncher.launch("image/*")
    }

    val weatherRepo = remember { WeatherRepository() }
    var weatherLoading by remember { mutableStateOf(false) }
    var weatherError by remember { mutableStateOf<String?>(null) }
    var weatherPreview by remember { mutableStateOf<ForecastItem?>(null) }

    fun pickClosestForecast(list: List<ForecastItem>, targetMillis: Long): ForecastItem? {
        if (list.isEmpty()) return null
        val targetSeconds = targetMillis / 1000L
        return list.filter { it.dt <= targetSeconds }.maxByOrNull { it.dt }
    }

    fun computeMillis(d: Long?, h: Int?, m: Int?): Long? {
        val dms = d ?: return null
        val hVal = h ?: 0
        val mVal = m ?: 0
        val cal = Calendar.getInstance().apply { timeInMillis = dms }
        cal.set(Calendar.HOUR_OF_DAY, hVal)
        cal.set(Calendar.MINUTE, mVal)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val notificationRepo = remember { NotificationRepository(db.notificationDao()) }
    val notificationRemoteRepo = remember { NotificationRemoteRepository() }
    val userUid = auth.getCurrentUser()?.uid ?: ""
    val unreadCount by notificationRepo.unreadCountFlow().collectAsState(initial = 0)

    LaunchedEffect(city, pickedDateMillis, pickedHour, pickedMinute) {
        val targetMillis =
            computeMillis(pickedDateMillis, pickedHour, pickedMinute) ?: return@LaunchedEffect

        if (city.isBlank()) {
            weatherPreview = null
            return@LaunchedEffect
        }

        weatherLoading = true
        weatherError = null

        val apiKey = ApiKeyProvider.getOpenWeatherKey(context)

        try {
            val coords = withContext(Dispatchers.IO) {
                weatherRepo.getCoordinates(city, apiKey)
            } ?: run {
                weatherError = "Cidade não encontrada"
                weatherLoading = false
                return@LaunchedEffect
            }

            val forecast = withContext(Dispatchers.IO) {
                weatherRepo.getForecast(coords.lat, coords.lon, apiKey)
            }

            val closest = pickClosestForecast(forecast.list, targetMillis)
                ?: run {
                    weatherError = "Sem previsão para esta data (máx: 5 dias)"
                    weatherLoading = false
                    return@LaunchedEffect
                }

            weatherPreview = closest

        } catch (e: Exception) {
            e.printStackTrace()
            weatherError = "Erro ao buscar previsão"
            weatherPreview = null
        } finally {
            weatherLoading = false
        }
    }

    val displayFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    suspend fun saveImageToInternal(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val file = File(context.filesDir, "event_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> input.copyTo(out) }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {

            val unread = appState.unreadCount.collectAsState(initial = 0).value

            val notifications by appState.notificationRepo
                .notificationsFlow()
                .collectAsState(initial = emptyList())

            TopAppBar(
                title = "criar evento",
                showBackButton = true,
                onBack = { navController.popBackStack() },
                notificationCount = unread,
                notifications = notifications,
                onNotificationClick = {
                    appState.nav.navigate("notifications")
                },
                appState = appState
            )
        },
        bottomBar = {
            BottomMenu(
                currentRoute = "event_list",
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(DARKBLUE, MEDIUMBLUE, MEDIUMBLUE, DARKBLUE, DARKBLUE)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = paddingValues.calculateTopPadding())
                    .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                    .background(Color(0xFFF0F0F0))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { pickImage() },
                        modifier = Modifier
                            .size(180.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF2F4F7),
                            contentColor = Color.Unspecified
                        ),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = MEDIUMBLUE,
                                    modifier = Modifier.size(34.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Adicionar foto",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    LabeledInputBase(
                        label = "Nome do evento",
                        value = name,
                        placeholder = "Digite o nome do evento",
                        onValueChange = { name = it }
                    )

                    Spacer(Modifier.height(10.dp))

                    LabeledInputBase(
                        label = "Descrição",
                        value = description,
                        placeholder = "Adicione detalhes (opcional)",
                        onValueChange = { description = it },
                        singleLine = false,
                        maxLines = 4
                    )

                    Spacer(Modifier.height(10.dp))

                    LabeledInputBase(
                        label = "Local",
                        value = location,
                        placeholder = "Digite o local",
                        onValueChange = { location = it }
                    )

                    Spacer(Modifier.height(10.dp))

                    LabeledInputBase(
                        label = "Cidade",
                        value = city,
                        placeholder = "Digite a cidade",
                        onValueChange = { city = it }
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {

                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                        ) {

                            Text(
                                text = "Orçamento",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = DARKBLUE
                            )

                            Spacer(Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(66.dp),
                                contentAlignment = Alignment.TopStart
                            ) {
                                MoneyInputFieldOnly(
                                    rawValue = rawBudget,
                                    onValueChange = { digits ->
                                        rawBudget = digits
                                        val doubleValue = digits.toDoubleOrNull() ?: 0.0
                                        budgetMasked = "R$ " +
                                                String.format("%.2f", doubleValue / 100)
                                                    .replace(".", ",")
                                    }
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {

                            Text(
                                text = "Data e hora",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = DARKBLUE
                            )

                            Spacer(Modifier.height(6.dp))

                            val millis = computeMillis(pickedDateMillis, pickedHour, pickedMinute)
                            val dateTimeText =
                                millis?.let { displayFormat.format(Date(it)) } ?: "Selecione"

                            OutlinedTextField(
                                value = dateTimeText,
                                onValueChange = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledContainerColor = Color.Transparent,
                                    disabledTextColor = Color.Gray,
                                    disabledBorderColor = Color.Gray
                                ),
                                trailingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.offset(x = 12.dp)
                                    ) {
                                        IconButton(onClick = { showDatePicker = true }) {
                                            Icon(
                                                Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = MEDIUMBLUE
                                            )
                                        }

                                        IconButton(
                                            onClick = { showTimePicker = true },
                                            modifier = Modifier.offset(x = (-16).dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = MEDIUMBLUE
                                            )
                                        }
                                    }
                                }
                            )

                            if (millis != null && millis <= System.currentTimeMillis()) {
                                Text(
                                    "Selecione uma data futura",
                                    color = Color(0xFFDC2626),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (weatherLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Buscando previsão...", color = Color.Gray)
                        }
                    } else {
                        weatherError?.let { err ->
                            Text(err, color = Color(0xFFDC2626), fontSize = 12.sp)
                        }

                        weatherPreview?.let { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(2.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val iconUrl =
                                        "https://openweathermap.org/img/wn/${item.weather.firstOrNull()?.icon}@2x.png"
                                    Image(
                                        painter = rememberAsyncImagePainter(iconUrl),
                                        contentDescription = null,
                                        modifier = Modifier.size(42.dp)
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = item.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercaseChar() }
                                                ?: "—",
                                            fontSize = 16.sp,
                                            color = Color(0xFF111827),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Temp: ${"%.1f".format(item.main.temp)}°C  •  Umid.: ${item.main.humidity}%",
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        val forecastDate = Date(item.dt * 1000)
                                        val sdf =
                                            SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                                        Text(
                                            "Previsão para: ${sdf.format(forecastDate)}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {

                                if (name.isBlank() || location.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Preencha nome e local.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@launch
                                }

                                isSaving = true

                                val internalFile = imageUri?.let { saveImageToInternal(it) }
                                val finalImagePath = internalFile?.absolutePath
                                val finalBudget = (rawBudget.toDoubleOrNull() ?: 0.0) / 100

                                val event = Event(
                                    id = 0,
                                    userUid = auth.getCurrentUser()?.uid ?: "",
                                    name = name,
                                    description = description,
                                    location = "$location, $city",
                                    budget = finalBudget,
                                    dateTime = computeMillis(
                                        pickedDateMillis,
                                        pickedHour,
                                        pickedMinute
                                    ) ?: 0L,
                                    imageUri = finalImagePath,
                                    weatherTemp = weatherPreview?.main?.temp?.toDouble(),
                                    weatherDesc = weatherPreview?.weather?.firstOrNull()?.description,
                                    weatherIcon = weatherPreview?.weather?.firstOrNull()?.icon,
                                    weatherFeelsLike = weatherPreview?.main?.feels_like?.toDouble(),
                                    weatherHumidity = weatherPreview?.main?.humidity,
                                    weatherWindSpeed = weatherPreview?.wind?.speed?.toDouble()
                                )

                                val eventId: Long = withContext(Dispatchers.IO) {
                                    repository.insertEvent(event, finalImagePath)
                                }

                                val notif = NotificationEntity(
                                    title = "Evento criado!",
                                    message = "Seu evento \"$name\" foi salvo com sucesso!",
                                    timestamp = System.currentTimeMillis(),
                                    isRead = false,
                                    eventId = eventId
                                )

                                withContext(Dispatchers.IO) {
                                    notificationRepo.insert(notif)
                                }

                                withContext(Dispatchers.IO) {
                                    notificationRemoteRepo.createNotification(
                                        userUid,
                                        mapOf(
                                            "title" to notif.title,
                                            "message" to notif.message,
                                            "timestamp" to notif.timestamp,
                                            "eventId" to eventId
                                        )
                                    )
                                }

                                NotificationHelper.showNotification(
                                    context = context,
                                    title = notif.title,
                                    message = notif.message
                                )

                                isSaving = false
                                Toast.makeText(context, "Evento salvo!", Toast.LENGTH_SHORT).show()

                                navController.popBackStack()
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MEDIUMBLUE,
                            contentColor = Color.White
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("salvar evento", fontSize = 17.sp)
                        }
                    }

                    Spacer(Modifier.height(120.dp))
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { pickedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton({ showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = state) }
    }

    if (showTimePicker) {
        val now = Calendar.getInstance()
        val h = pickedHour ?: now.get(Calendar.HOUR_OF_DAY)
        val m = pickedMinute ?: now.get(Calendar.MINUTE)

        LaunchedEffect(Unit) {
            showTimePicker = false
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    pickedHour = hour
                    pickedMinute = minute
                },
                h,
                m,
                true
            ).show()
        }
    }
}

@Composable
fun MoneyInputFieldOnly(
    rawValue: String,
    onValueChange: (String) -> Unit
) {
    val digitsOnly = rawValue.filter { it.isDigit() }
    val formattedValue = formatMoney(digitsOnly)

    OutlinedTextField(
        value = formattedValue,
        onValueChange = { newText ->
            val digits = newText.filter { it.isDigit() }
            onValueChange(digits)
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        placeholder = {
            Text("R$ 0,00", color = Color.Gray)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MEDIUMBLUE,
            unfocusedBorderColor = Color.Gray,
            cursorColor = MEDIUMBLUE,
            focusedPlaceholderColor = Color.Transparent,
            unfocusedPlaceholderColor = Color.Transparent
        )
    )

    if (digitsOnly.isEmpty()) {
        Text(
            text = "R$ 0,00",
            color = Color.Gray,
            modifier = Modifier
                .padding(start = 16.dp, top = 17.dp)
        )
    }
}