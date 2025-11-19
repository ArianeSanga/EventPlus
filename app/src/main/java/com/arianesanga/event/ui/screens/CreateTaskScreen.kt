package com.arianesanga.event.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.Task
import com.arianesanga.event.data.remote.repository.TaskRemoteRepository
import com.arianesanga.event.data.repository.TaskRepository
import com.arianesanga.event.ui.components.AppState
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    navController: NavController,
    eventId: Int,
    appState: AppState
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { TaskRepository(db.taskDao(), TaskRemoteRepository()) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            val unread = appState.unreadCount.collectAsState(initial = 0).value

            val notifications by appState.notificationRepo
                .notificationsFlow()
                .collectAsState(initial = emptyList())
            TopAppBar(
                title = "criar nova tarefa",
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
                        Brush.verticalGradient(
                            listOf(DARKBLUE, MEDIUMBLUE, MEDIUMBLUE, DARKBLUE)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                    .background(Color(0xFFF0F0F0))
            ) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {

                    Spacer(Modifier.height(10.dp))

                    LabeledInputBase(
                        label = "Título",
                        value = title,
                        placeholder = "Digite o título da tarefa",
                        onValueChange = { title = it }
                    )

                    LabeledInputBase(
                        label = "Descrição",
                        value = description,
                        placeholder = "Adicione detalhes (opcional)",
                        onValueChange = { description = it },
                        singleLine = false,
                        maxLines = 4
                    )

                    LabeledInputMaskedMoney(
                        label = "Valor",
                        rawValue = valueText,
                        onValueChange = { valueText = it }
                    )

                    Text(
                        "Status da tarefa",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DARKBLUE,
                        modifier = Modifier.padding(bottom = 0.dp)
                    )

                    StatusSelector(
                        status = status,
                        onStatusChange = { status = it }
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Informe o título da tarefa.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val value = (valueText.toDoubleOrNull() ?: 0.0) / 100

                            val task = Task(
                                eventId = eventId,
                                title = title,
                                description = description.ifBlank { null },
                                value = value,
                                status = status
                            )

                            scope.launch(Dispatchers.IO) {
                                repo.insertTask(task)
                                launch(Dispatchers.Main) {
                                    Toast.makeText(context, "Tarefa criada!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MEDIUMBLUE,
                            contentColor = Color.White
                        )
                    ) {
                        Text("salvar tarefa", fontSize = 18.sp)
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun StatusSelector(
    status: Int,
    onStatusChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
    StatusOption(
            label = "Pendente",
            selected = status == 0,
            color = Color(0xFFDC2626),
            onClick = { onStatusChange(0) }
        )
        StatusOption(
            label = "Andamento",
            selected = status == 1,
            color = Color(0xFFF59E0B),
            onClick = { onStatusChange(1) }
        )
        StatusOption(
            label = "Concluída",
            selected = status == 2,
            color = Color(0xFF16A34A),
            onClick = { onStatusChange(2) }
        )
    }
}

@Composable
fun StatusOption(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {

    val bg = if (selected) color.copy(alpha = 0.12f) else Color(0xFFe6e6e6)

    Box(
        modifier = Modifier
            .width(110.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .then(
                if (selected)
                    Modifier.border(
                        width = 1.dp,
                        color = color,
                        shape = RoundedCornerShape(12.dp)
                    )
                else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

fun formatMoney(input: String): String {
    val digits = input.filter { it.isDigit() }

    if (digits.isEmpty()) return "R$ 0,00"

    val value = digits.toLong()

    val formatted = "%,.2f".format(value / 100.0)
        .replace(',', 'X')
        .replace('.', ',')
        .replace('X', '.')

    return "R$ $formatted"
}

@Composable
fun LabeledInputMaskedMoney(
    label: String,
    rawValue: String,
    onValueChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DARKBLUE
        )

        Spacer(Modifier.height(6.dp))

        val digitsOnly = rawValue.filter { it.isDigit() }
        val formattedValue = if (digitsOnly.isEmpty()) "" else formatMoney(digitsOnly)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {

            OutlinedTextField(
                value = formattedValue,
                onValueChange = { newText ->
                    val digits = newText.filter { it.isDigit() }
                    onValueChange(digits)
                },
                placeholder = {
                    Text(
                        text = "R$ 0,00 (opcional)",
                        color = Color.Gray
                    )
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.fillMaxSize(),
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
}

@Composable
fun LabeledInputBase(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    maxLines: Int = 1,
) {
    Column(Modifier.fillMaxWidth()) {

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DARKBLUE
        )

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black,
                fontSize = 15.sp
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = singleLine,
            maxLines = maxLines,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MEDIUMBLUE,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MEDIUMBLUE
            )
        )
    }
}
