package com.arianesanga.event.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.arianesanga.event.data.remote.repository.EventRemoteRepository
import com.arianesanga.event.data.remote.repository.WeatherRepository
import com.arianesanga.event.data.repository.EventRepository
import com.arianesanga.event.ui.components.AppState
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import com.arianesanga.event.utils.ApiKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    eventId: Int,
    appState: AppState
) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    val db = remember { AppDatabase.getInstance(context) }
    val dao = db.eventDao()
    val repository = remember { EventRepository(dao, EventRemoteRepository()) }

    var loadedEvent by remember { mutableStateOf<Event?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    var currentImageUrl by remember { mutableStateOf<String?>(null) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    var pickedDateMillis by remember { mutableStateOf<Long?>(null) }
    var pickedHour by remember { mutableStateOf<Int?>(null) }
    var pickedMinute by remember { mutableStateOf<Int?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val displayFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    var city by remember { mutableStateOf("") }
    var weatherPreview by remember { mutableStateOf<ForecastItem?>(null) }
    var weatherLoading by remember { mutableStateOf(false) }
    var weatherError by remember { mutableStateOf<String?>(null) }
    val weatherRepo = remember { WeatherRepository() }

    fun computeDateTimeMillis(): Long? {
        val d = pickedDateMillis ?: return null
        val h = pickedHour ?: 0
        val m = pickedMinute ?: 0
        val cal = Calendar.getInstance().apply { timeInMillis = d }
        cal.set(Calendar.HOUR_OF_DAY, h)
        cal.set(Calendar.MINUTE, m)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    LaunchedEffect(eventId) {
        coroutine.launch(Dispatchers.IO) {
            val ev = dao.getEventById(eventId)
            if (ev != null) {
                loadedEvent = ev
                name = ev.name
                description = ev.description

                val parts = ev.location.split(",")
                location = parts.getOrNull(0)?.trim() ?: ""
                city = parts.getOrNull(1)?.trim() ?: ""

                budget = ((ev.budget * 100).toInt()).toString()
                currentImageUrl = ev.imageUri

                val cal = Calendar.getInstance().apply { timeInMillis = ev.dateTime }
                pickedDateMillis = cal.timeInMillis
                pickedHour = cal.get(Calendar.HOUR_OF_DAY)
                pickedMinute = cal.get(Calendar.MINUTE)
            }
        }
    }

    LaunchedEffect(city, pickedDateMillis, pickedHour, pickedMinute) {
        val targetMillis = computeMillis(pickedDateMillis, pickedHour, pickedMinute)
            ?: return@LaunchedEffect

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
            weatherPreview = closest

        } catch (e: Exception) {
            weatherError = "Erro ao buscar previsão"
        } finally {
            weatherLoading = false
        }
    }

    if (loadedEvent == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            val unread = appState.unreadCount.collectAsState(initial = 0).value

            val notifications by appState.notificationRepo
                .notificationsFlow()
                .collectAsState(initial = emptyList())
            TopAppBar(
                title = "editar evento",
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
    ) {
        EditEventContent(
            padding = it,
            name = name, onName = { name = it },
            description = description, onDescription = { description = it },
            location = location, onLocation = { location = it },
            city = city, onCity = { city = it },
            budget = budget, onBudget = { budget = it },
            currentImageUrl = currentImageUrl,
            newImageUri = newImageUri,
            onPickImage = { uri -> newImageUri = uri },
            displayFormat = displayFormat,
            computeDateTimeMillis = { computeDateTimeMillis() },
            showDatePicker = showDatePicker, setShowDatePicker = { showDatePicker = it },
            showTimePicker = showTimePicker, setShowTimePicker = { showTimePicker = it },
            weatherLoading = weatherLoading,
            weatherPreview = weatherPreview,
            weatherError = weatherError,
            onSave = {
                val dt = computeDateTimeMillis()
                if (dt == null) {
                    Toast.makeText(context, "Selecione data e hora", Toast.LENGTH_SHORT).show()
                    return@EditEventContent
                }

                val updated = loadedEvent!!.copy(
                    name = name,
                    description = description,
                    location = "$location, $city",
                    budget = (budget.toDoubleOrNull() ?: 0.0) / 100,
                    weatherTemp = weatherPreview?.main?.temp?.toDouble(),
                    weatherDesc = weatherPreview?.weather?.firstOrNull()?.description,
                    weatherIcon = weatherPreview?.weather?.firstOrNull()?.icon,
                    weatherFeelsLike = weatherPreview?.main?.feels_like?.toDouble(),
                    weatherHumidity = weatherPreview?.main?.humidity,
                    weatherWindSpeed = weatherPreview?.wind?.speed?.toDouble(),
                    dateTime = dt
                )

                coroutine.launch(Dispatchers.IO) {
                    repository.updateEvent(updated, newImageUri)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Evento atualizado!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
            }
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton({
                    state.selectedDateMillis?.let { pickedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton({ showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state)
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, h, m ->
                pickedHour = h
                pickedMinute = m
            },
            pickedHour ?: cal.get(Calendar.HOUR_OF_DAY),
            pickedMinute ?: cal.get(Calendar.MINUTE),
            true
        ).apply {
            setOnDismissListener { showTimePicker = false }
        }.show()
    }
}

@Composable
fun EditEventContent(
    padding: PaddingValues,
    name: String, onName: (String) -> Unit,
    description: String, onDescription: (String) -> Unit,
    location: String, onLocation: (String) -> Unit,
    city: String, onCity: (String) -> Unit,
    budget: String, onBudget: (String) -> Unit,
    currentImageUrl: String?,
    newImageUri: Uri?,
    onPickImage: (Uri?) -> Unit,
    displayFormat: SimpleDateFormat,
    computeDateTimeMillis: () -> Long?,
    showDatePicker: Boolean,
    setShowDatePicker: (Boolean) -> Unit,
    showTimePicker: Boolean,
    setShowTimePicker: (Boolean) -> Unit,
    weatherLoading: Boolean,
    weatherPreview: ForecastItem?,
    weatherError: String?,
    onSave: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(DARKBLUE, MEDIUMBLUE, MEDIUMBLUE, DARKBLUE)
                    )
                )
        )

        Box(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                .background(Color(0xFFF0F0F0))
        ) {

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(14.dp))

                EventPhotoSection(currentImageUrl, newImageUri, onPickImage)

                EventMainFields(
                    name = name,
                    onName = onName,
                    description = description,
                    onDescription = onDescription,
                    location = location,
                    onLocation = onLocation,
                    city = city,
                    onCity = onCity
                )

                EventBudgetAndDateTimeSection(
                    budget = budget,
                    onBudget = onBudget,
                    displayFormat = displayFormat,
                    computeDateTimeMillis = computeDateTimeMillis,
                    showDatePicker = showDatePicker,
                    setShowDatePicker = setShowDatePicker,
                    showTimePicker = showTimePicker,
                    setShowTimePicker = setShowTimePicker
                )

                Spacer(Modifier.height(18.dp))

                WeatherPreview(weatherLoading, weatherError, weatherPreview)

                Spacer(Modifier.height(18.dp))

                SaveEventButton(onSave)

                Spacer(Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun EventPhotoSection(
    currentImageUrl: String?,
    newImageUri: Uri?,
    onPickImage: (Uri?) -> Unit
) {
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> onPickImage(uri) }

    Box(
        modifier = Modifier
            .size(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE2E8F0))
            .pointerInput(Unit) {
                detectTapGestures { picker.launch("image/*") }
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            newImageUri != null -> Image(
                painter = rememberAsyncImagePainter(newImageUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            currentImageUrl != null -> Image(
                painter = rememberAsyncImagePainter(currentImageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            else -> Icon(
                Icons.Default.AddAPhoto,
                contentDescription = null,
                tint = MEDIUMBLUE,
                modifier = Modifier.size(42.dp)
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    Text(
        text = "Toque para trocar a foto",
        fontSize = 14.sp,
        color = Color.Gray
    )

    Spacer(Modifier.height(20.dp))
}

@Composable
fun EventMainFields(
    name: String, onName: (String) -> Unit,
    description: String, onDescription: (String) -> Unit,
    location: String, onLocation: (String) -> Unit,
    city: String, onCity: (String) -> Unit
) {
    LabeledInputBase("Nome do evento", name, "Digite o nome", onName)
    Spacer(Modifier.height(10.dp))

    LabeledInputBase("Descrição", description, "Adicione detalhes", onDescription, singleLine = false, maxLines = 4)
    Spacer(Modifier.height(10.dp))

    LabeledInputBase("Local", location, "Digite o local", onLocation)
    Spacer(Modifier.height(10.dp))

    LabeledInputBase("Cidade", city, "Digite a cidade", onCity)
    Spacer(Modifier.height(10.dp))
}

@Composable
fun EventBudgetAndDateTimeSection(
    budget: String,
    onBudget: (String) -> Unit,
    displayFormat: SimpleDateFormat,
    computeDateTimeMillis: () -> Long?,
    showDatePicker: Boolean,
    setShowDatePicker: (Boolean) -> Unit,
    showTimePicker: Boolean,
    setShowTimePicker: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        EventBudgetInput(
            budget = budget,
            onBudget = onBudget,
            modifier = Modifier.weight(0.3f)
        )

        EventDateTimeInput(
            displayFormat = displayFormat,
            computeDateTimeMillis = computeDateTimeMillis,
            setShowDatePicker = setShowDatePicker,
            setShowTimePicker = setShowTimePicker,
            modifier = Modifier.weight(0.7f)
        )
    }
}

@Composable
fun EventBudgetInput(
    budget: String,
    onBudget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Orçamento",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DARKBLUE
        )
        Spacer(Modifier.height(6.dp))

        val digitsOnly = budget.filter { it.isDigit() }
        val formattedBudget = formatMoney(digitsOnly)

        OutlinedTextField(
            value = formattedBudget,
            onValueChange = { newText ->
                onBudget(newText.filter { it.isDigit() })
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
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

@Composable
fun EventDateTimeInput(
    displayFormat: SimpleDateFormat,
    computeDateTimeMillis: () -> Long?,
    setShowDatePicker: (Boolean) -> Unit,
    setShowTimePicker: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier){

        Text(
            text = "Data e hora",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DARKBLUE
        )

        Spacer(Modifier.height(6.dp))

        val dtMillis = computeDateTimeMillis()
        val dateText = dtMillis?.let { displayFormat.format(Date(it)) } ?: "Selecione"

        OutlinedTextField(
            value = dateText,
            enabled = false,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.Gray,
                disabledContainerColor = Color.Transparent
            ),
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    IconButton(
                        onClick = { setShowDatePicker(true) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MEDIUMBLUE
                        )
                    }

                    IconButton(
                        onClick = { setShowTimePicker(true) },
                        modifier = Modifier.size(36.dp)
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
    }
}

@Composable
fun WeatherPreview(loading: Boolean, error: String?, preview: ForecastItem?) {
    when {
        loading -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text("Carregando previsão...", color = Color.Gray)
        }

        error != null -> Text(error, color = Color.Red, fontSize = 12.sp)

        preview != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MEDIUMBLUE)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preview.weather.firstOrNull()?.description
                                ?.replaceFirstChar { it.uppercase() } ?: "—",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Temp: ${preview.main.temp}°C • Umidade: ${preview.main.humidity}%",
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                    }

                    val iconUrl = "https://openweathermap.org/img/wn/${preview.weather.firstOrNull()?.icon}@2x.png"

                    Image(
                        painter = rememberAsyncImagePainter(iconUrl),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SaveEventButton(onSave: () -> Unit) {
    Button(
        onClick = onSave,
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MEDIUMBLUE,
            contentColor = Color.White
        )
    ) {
        Text("atualizar evento", fontSize = 17.sp)
    }
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

fun pickClosestForecast(list: List<ForecastItem>, targetMillis: Long): ForecastItem? {
    if (list.isEmpty()) return null
    val targetSeconds = targetMillis / 1000L
    return list.filter { it.dt <= targetSeconds }.maxByOrNull { it.dt }
}