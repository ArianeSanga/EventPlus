package com.arianesanga.event.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.ui.components.AppState
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    navController: NavController,
    appState: AppState
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val eventDao = db.eventDao()
    val scope = rememberCoroutineScope()
    val auth = remember { AuthService() }

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    var allEvents by remember { mutableStateOf(emptyList<Event>()) }
    var query by remember { mutableStateOf("") }

    var sheetDateFilter by remember { mutableStateOf(DateFilterOption.ALL) }
    var sheetCustomDate by remember { mutableStateOf<Long?>(null) }
    var sheetMinBudget by remember { mutableStateOf("") }
    var sheetMaxBudget by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    var appliedDateFilter by remember { mutableStateOf(DateFilterOption.ALL) }
    var appliedCustomDate by remember { mutableStateOf<Long?>(null) }
    var appliedMinBudget by remember { mutableStateOf<Double?>(null) }
    var appliedMaxBudget by remember { mutableStateOf<Double?>(null) }

    val filteredEvents by remember(
        allEvents, query,
        appliedDateFilter, appliedCustomDate,
        appliedMinBudget, appliedMaxBudget
    ) {
        derivedStateOf {
            allEvents.filter { ev ->

                val matchesQuery = ev.name.contains(query, true)

                val matchesBudget =
                    when {
                        appliedMinBudget != null && appliedMaxBudget != null ->
                            ev.budget in appliedMinBudget!!..appliedMaxBudget!!

                        appliedMinBudget != null ->
                            ev.budget >= appliedMinBudget!!

                        appliedMaxBudget != null ->
                            ev.budget <= appliedMaxBudget!!

                        else -> true
                    }

                val matchesDate =
                    when (appliedDateFilter) {
                        DateFilterOption.ALL -> true
                        DateFilterOption.TODAY -> isSameDay(ev.dateTime, Date())
                        DateFilterOption.THIS_WEEK -> isSameWeek(ev.dateTime, Date())
                        DateFilterOption.THIS_MONTH -> isSameMonth(ev.dateTime, Date())
                        DateFilterOption.CUSTOM ->
                            appliedCustomDate?.let { isSameDay(ev.dateTime, it) } ?: true
                    }

                matchesQuery && matchesBudget && matchesDate
            }
        }
    }

    fun reload() {
        scope.launch(Dispatchers.IO) {
            val uid = auth.getCurrentUser()?.uid
            val list = if (uid != null) eventDao.getEventsByUser(uid) else emptyList()

            launch(Dispatchers.Main) {
                allEvents = list
            }
        }
    }

    LaunchedEffect(Unit) { reload() }
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { reload() }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutine = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            val unread = appState.unreadCount.collectAsState(initial = 0).value

            val notifications by appState.notificationRepo
                .notificationsFlow()
                .collectAsState(initial = emptyList())
            TopAppBar(
                title = "meus eventos",
                showBackButton = false,
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_event") },
                modifier = Modifier
                    .size(70.dp)
                    .offset(y = 15.dp),
                shape = RoundedCornerShape(20.dp),
                containerColor = MEDIUMBLUE,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Criar evento", Modifier.size(40.dp))
            }
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
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                    .background(Color(0xFFf0f0f0))
            ) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize()
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = {
                                Text(
                                    "Buscar por nome",
                                    color = Color.Gray
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MEDIUMBLUE
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MEDIUMBLUE,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = MEDIUMBLUE,
                                focusedLeadingIconColor = MEDIUMBLUE,
                                unfocusedLeadingIconColor = MEDIUMBLUE,
                                focusedPlaceholderColor = Color.Gray,
                                unfocusedPlaceholderColor = Color.Gray
                            )
                        )

                        Spacer(Modifier.width(10.dp))

                        Button(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier.height(55.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MEDIUMBLUE,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.FilterList, null)
                            Spacer(Modifier.width(7.dp))
                            Text("Filtrar", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (filteredEvents.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhum evento encontrado.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filteredEvents, key = { it.id }) { ev ->

                                EventCompactCard(
                                    event = ev,
                                    onClickDetails = { navController.navigate("event_details/$it") }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.35f)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 1.dp, topEnd = 10.dp))
                        .background(Color.White)
                        .padding(20.dp)
                ) {

                    Text(
                        "Filtrar eventos",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DARKBLUE
                    )

                    Spacer(Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF7F7F7))
                            .padding(16.dp)
                    ) {

                        Text(
                            "Data",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DARKBLUE
                        )

                        Spacer(Modifier.height(10.dp))

                        var showPickerCustom by remember { mutableStateOf(false) }
                        var expanded by remember { mutableStateOf(false) }

                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MEDIUMBLUE),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MEDIUMBLUE
                            )
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MEDIUMBLUE)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                sheetDateFilter.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(
                                    Color(0xFFF3F3F3),
                                    RoundedCornerShape(14.dp)
                                )
                                .width(150.dp)
                        ) {
                            DateFilterOption.values().forEachIndexed { index, opt ->

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (sheetDateFilter == opt)
                                                Color(0xFFE2EAFB)
                                            else
                                                Color.Transparent
                                        )
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures {
                                                sheetDateFilter = opt
                                                if (opt != DateFilterOption.CUSTOM) sheetCustomDate = null
                                                expanded = false
                                            }
                                        }
                                ) {
                                    Text(
                                        opt.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (sheetDateFilter == opt) FontWeight.Bold else FontWeight.Medium,
                                        color = if (sheetDateFilter == opt) MEDIUMBLUE else Color.Black
                                    )
                                }

                                if (index < DateFilterOption.values().size - 1) {
                                    Divider(
                                        color = Color(0xFFE0E0E0),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    )
                                }
                            }
                        }

                        if (sheetDateFilter == DateFilterOption.CUSTOM) {
                            Spacer(Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { showPickerCustom = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MEDIUMBLUE
                                ),
                                border = BorderStroke(1.dp, MEDIUMBLUE)
                            ) {
                                Icon(Icons.Default.EditCalendar, contentDescription = null, tint = MEDIUMBLUE)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    sheetCustomDate?.let {
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            .format(Date(it))
                                    } ?: "Selecionar data",
                                    fontSize = 15.sp
                                )
                            }

                            if (showPickerCustom) {
                                DatePickerDialog(
                                    onDateSelected = {
                                        sheetCustomDate = it
                                        showPickerCustom = false
                                    },
                                    onDismiss = { showPickerCustom = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        TextButton(
                            onClick = {
                                sheetDateFilter = DateFilterOption.ALL
                                sheetCustomDate = null
                                sheetMinBudget = ""
                                sheetMaxBudget = ""
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MEDIUMBLUE
                            )
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = MEDIUMBLUE)
                            Spacer(Modifier.width(6.dp))
                            Text("Limpar filtros", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }

                        Row {

                            TextButton(
                                onClick = { showFilterSheet = false },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MEDIUMBLUE
                                )
                            ) {
                                Text("Fechar", fontSize = 15.sp)
                            }

                            Spacer(Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    appliedDateFilter = sheetDateFilter
                                    appliedCustomDate = sheetCustomDate
                                    appliedMinBudget = sheetMinBudget.toDoubleOrNull()
                                    appliedMaxBudget = sheetMaxBudget.toDoubleOrNull()
                                    coroutine.launch { showFilterSheet = false }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MEDIUMBLUE,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Aplicar filtros", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun EventCompactCard(
    event: Event,
    onClickDetails: (Int) -> Unit
) {
    val format = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val painterModel = remember(event.imageUri) {
        when {
            event.imageUri.isNullOrBlank() -> null
            event.imageUri.startsWith("content://") -> Uri.parse(event.imageUri)
            event.imageUri.startsWith("/") -> Uri.fromFile(File(event.imageUri))
            event.imageUri.startsWith("http") -> event.imageUri
            else -> null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.Top
        ) {

            Box(
                modifier = Modifier
                    .size(115.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE9EEF7)),
                contentAlignment = Alignment.Center
            ) {
                if (painterModel != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = painterModel),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        event.name.take(1).uppercase(),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF394867)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .height(115.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            event.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            format.format(Date(event.dateTime)),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    if (event.weatherIcon != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    "https://openweathermap.org/img/wn/${event.weatherIcon}.png"
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(55.dp)
                                    .offset(y = (-8).dp)
                            )

                            Text(
                                "${"%.0f".format(event.weatherTemp)}°",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.offset(y = (-16).dp, x = 2.dp)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { onClickDetails(event.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MEDIUMBLUE),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MEDIUMBLUE
                    ),
                ) {
                    Text("Ver detalhes", fontSize = 13.sp)
                }
            }
        }
    }
}

private fun Long.toCalendar(): Calendar =
    Calendar.getInstance().apply { timeInMillis = this@toCalendar }

private fun isSameDay(eventMillis: Long, target: Date): Boolean {
    val ev = eventMillis.toCalendar()
    val tgt = Calendar.getInstance().apply { time = target }
    return ev.get(Calendar.YEAR) == tgt.get(Calendar.YEAR) &&
            ev.get(Calendar.DAY_OF_YEAR) == tgt.get(Calendar.DAY_OF_YEAR)
}

private fun isSameDay(eventMillis: Long, targetMillis: Long): Boolean {
    val ev = eventMillis.toCalendar()
    val tgt = targetMillis.toCalendar()
    return ev.get(Calendar.YEAR) == tgt.get(Calendar.YEAR) &&
            ev.get(Calendar.DAY_OF_YEAR) == tgt.get(Calendar.DAY_OF_YEAR)
}

private fun isSameWeek(eventMillis: Long, target: Date): Boolean {
    val ev = eventMillis.toCalendar()
    val tgt = Calendar.getInstance().apply { time = target }
    return ev.get(Calendar.YEAR) == tgt.get(Calendar.YEAR) &&
            ev.get(Calendar.WEEK_OF_YEAR) == tgt.get(Calendar.WEEK_OF_YEAR)
}

private fun isSameMonth(eventMillis: Long, target: Date): Boolean {
    val ev = eventMillis.toCalendar()
    val tgt = Calendar.getInstance().apply { time = target }
    return ev.get(Calendar.YEAR) == tgt.get(Calendar.YEAR) &&
            ev.get(Calendar.MONTH) == tgt.get(Calendar.MONTH)
}

enum class DateFilterOption(val displayName: String) {
    ALL("Todos"),
    TODAY("Hoje"),
    THIS_WEEK("Esta semana"),
    THIS_MONTH("Este mês"),
    CUSTOM("Escolher data");

    companion object {
        fun fromDisplayName(name: String): DateFilterOption? =
            values().firstOrNull { it.displayName == name }
    }
}

@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val today = remember { Calendar.getInstance() }

    var selectedDate by remember { mutableStateOf(today.clone() as Calendar) }
    var currentMonth by remember { mutableStateOf(today.clone() as Calendar) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        .format(currentMonth.time),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        currentMonth.add(Calendar.MONTH, -1)
                    }) { Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color.Black) }

                    IconButton(onClick = {
                        currentMonth.add(Calendar.MONTH, 1)
                    }) { Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Black) }
                }

                Spacer(Modifier.height(8.dp))

                val daysOfWeek = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    daysOfWeek.forEach {
                        Text(
                            it,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                CalendarGrid(
                    month = currentMonth,
                    selectedDate = selectedDate,
                    onDaySelected = { day ->
                        selectedDate = (currentMonth.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, day)
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MEDIUMBLUE)
                    }
                    TextButton(onClick = {
                        onDateSelected(selectedDate.timeInMillis)
                    }) {
                        Text("OK", color = MEDIUMBLUE, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    month: Calendar,
    selectedDate: Calendar,
    onDaySelected: (Int) -> Unit
) {
    val calendar = month.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val rows = mutableListOf<List<Int?>>()
    var currentRow = MutableList<Int?>(7) { null }

    var col = firstDayOfWeek
    for (day in 1..maxDays) {
        currentRow[col] = day
        col++

        if (col == 7) {
            rows.add(currentRow.toList())
            currentRow = MutableList(7) { null }
            col = 0
        }
    }
    if (currentRow.any { it != null }) rows.add(currentRow)

    Column {
        rows.forEach { week ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { day ->
                    val tapModifier = if (day != null) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onDaySelected(day)
                                }
                            )
                        }
                    } else Modifier

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(50))
                            .then(tapModifier),
                        contentAlignment = Alignment.Center
                    ) {

                        val isSelected = day != null &&
                                selectedDate.get(Calendar.DAY_OF_MONTH) == day &&
                                selectedDate.get(Calendar.MONTH) == month.get(Calendar.MONTH) &&
                                selectedDate.get(Calendar.YEAR) == month.get(Calendar.YEAR)

                        if (day != null) {
                            Box(
                                modifier = if (isSelected) {
                                    Modifier
                                        .size(38.dp)
                                        .background(MEDIUMBLUE, RoundedCornerShape(50))
                                } else Modifier,
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}