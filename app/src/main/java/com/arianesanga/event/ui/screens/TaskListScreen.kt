package com.arianesanga.event.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
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
fun TaskListScreen(
    navController: NavController,
    eventId: Int,
    appState: AppState
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val unread = appState.unreadCount.collectAsState(initial = 0).value

    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { TaskRepository(db.taskDao(), TaskRemoteRepository()) }

    var eventBudget by remember { mutableStateOf(0.0) }

    LaunchedEffect(eventId) {
        scope.launch(Dispatchers.IO) {
            val ev = db.eventDao().getEventById(eventId)
            launch(Dispatchers.Main) { eventBudget = ev?.budget ?: 0.0 }
        }
    }

    var tasks by remember { mutableStateOf(emptyList<Task>()) }

    fun load() {
        scope.launch(Dispatchers.IO) {
            val data = repo.getTasksByEvent(eventId)
            launch(Dispatchers.Main) { tasks = data }
        }
    }

    LaunchedEffect(Unit) { load() }

    val pending = tasks.count { it.status == 0 }
    val inProgress = tasks.count { it.status == 1 }
    val completed = tasks.count { it.status == 2 }

    val completedValue = tasks.filter { it.status == 2 }.sumOf { it.value }

    val progressPercent =
        if (eventBudget > 0) (completedValue / eventBudget).coerceIn(0.0, 1.0) else 0.0

    val availableValue = (eventBudget - completedValue).coerceAtLeast(0.0)
    val percentColor = budgetColor(progressPercent)

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            val unread = appState.unreadCount.collectAsState(initial = 0).value

            val notifications by appState.notificationRepo
                .notificationsFlow()
                .collectAsState(initial = emptyList())
            TopAppBar(
                title = "tarefas e orçamento",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_task/$eventId") },
                modifier = Modifier
                    .size(70.dp)
                    .offset(y = 15.dp),
                shape = RoundedCornerShape(20.dp),
                containerColor = MEDIUMBLUE,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp))
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                DARKBLUE,
                                MEDIUMBLUE,
                                MEDIUMBLUE,
                                DARKBLUE
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                    .background(Color(0xFFF0F0F0))
            ) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize()
                ) {

                    Column(Modifier.fillMaxSize()) {

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IndicatorCard(
                                label = "Pendentes",
                                value = pending,
                                cardColor = Color(0xFFDC2626),
                                icon = Icons.Default.PendingActions,
                                valueColor = Color.Black
                            )

                            IndicatorCard(
                                label = "Em andamento",
                                value = inProgress,
                                cardColor = Color(0xFFF59E0B),
                                icon = Icons.Default.HourglassEmpty,
                                valueColor = Color.Black
                            )

                            IndicatorCard(
                                label = "Concluídas",
                                value = completed,
                                cardColor = Color(0xFF16A34A),
                                icon = Icons.Default.CheckCircle,
                                valueColor = Color.Black
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                            ) {

                                Text(
                                    "Controle financeiro",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Orçamento", color = Color.Gray, fontSize = 13.sp)
                                        Text(
                                            "R$ %.2f".format(eventBudget),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.Black
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Gasto", color = Color.Gray, fontSize = 13.sp)
                                        Text(
                                            "R$ %.2f".format(completedValue),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.Black
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Disponível", color = Color.Gray, fontSize = 13.sp)
                                        Text(
                                            "R$ %.2f".format(availableValue),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.Black
                                        )
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFE5E7EB))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progressPercent.toFloat())
                                            .clip(RoundedCornerShape(50))
                                            .background(percentColor)
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .background(
                                            color = percentColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(30.dp)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "%.0f%% do orçamento utilizado".format(progressPercent * 100),
                                        fontWeight = FontWeight.SemiBold,
                                        color = percentColor,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        if (tasks.isEmpty()) {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Nenhuma tarefa criada ainda.",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 120.dp)
                            ) {
                                items(tasks) { task ->
                                    TaskCard(
                                        task = task,
                                        onEditClick = {
                                            navController.navigate("edit_task/${task.id}")
                                        },
                                        onDeleteClick = { taskToDelete ->
                                            scope.launch(Dispatchers.IO) {
                                                repo.deleteTask(taskToDelete)
                                                launch(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Tarefa excluída!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    load()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IndicatorCard(
    label: String,
    value: Int,
    cardColor: Color,
    icon: ImageVector,
    valueColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
            .width(115.dp)
            .height(55.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = cardColor,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = value.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            }

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onEditClick: () -> Unit,
    onDeleteClick: (Task) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = task.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            if (!task.description.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                    Text(
                        text = task.description ?: "",
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )
                }
            }

            Divider(color = Color(0xFFE2E8F0))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                StatusBadge(task.status)

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "R$ %.2f".format(task.value),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = { onEditClick() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color(0xFF1E3A8A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDeleteClick(task) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: Int) {
    val (label, color) = when (status) {
        0 -> "Pendente" to Color(0xFFDC2626)
        1 -> "Em andamento" to Color(0xFFF59E0B)
        else -> "Concluída" to Color(0xFF16A34A)
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}