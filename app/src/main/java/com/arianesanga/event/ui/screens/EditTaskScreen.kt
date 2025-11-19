package com.arianesanga.event.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
fun EditTaskScreen(
    navController: NavController,
    taskId: Int,
    appState: AppState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { TaskRepository(db.taskDao(), TaskRemoteRepository()) }

    var task by remember { mutableStateOf<Task?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(0) }

    LaunchedEffect(taskId) {
        scope.launch(Dispatchers.IO) {
            val loaded = db.taskDao().getAllTasks().find { it.id == taskId }
            launch(Dispatchers.Main) {
                task = loaded
                loaded?.let {
                    title = it.title
                    description = it.description ?: ""
                    valueText = ((it.value * 100).toInt()).toString()
                    status = it.status
                }
            }
        }
    }

    if (task == null) {
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
                title = "editar tarefa",
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
    ) { padding ->

        Box(Modifier.fillMaxSize()) {

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(DARKBLUE, MEDIUMBLUE, MEDIUMBLUE, DARKBLUE)
                        )
                    )
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                    .background(Color(0xFFF0F0F0))
            ) {

                Column(
                    Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        color = DARKBLUE
                    )

                    StatusSelector(
                        status = status,
                        onStatusChange = { status = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {

                            val value = (valueText.toDoubleOrNull() ?: 0.0) / 100

                            val updated = task!!.copy(
                                title = title,
                                description = description.ifBlank { null },
                                value = value,
                                status = status
                            )

                            scope.launch(Dispatchers.IO) {
                                repo.updateTask(updated)
                                launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Tarefa atualizada!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MEDIUMBLUE,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("salvar alterações", fontSize = 18.sp)
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}