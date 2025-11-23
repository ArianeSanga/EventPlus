package com.arianesanga.event.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.data.remote.firebase.AuthService
import com.arianesanga.event.ui.components.AppState
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    appState: AppState
) {
    val auth = remember { AuthService() }
    val currentUserUid = auth.getCurrentUser()?.uid
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val eventDao = db.eventDao()
    val taskDao = db.taskDao()

    // 🍃 Nome do usuário real (com fallback inteligente)
    val firestore = remember { FirebaseFirestore.getInstance() }
    var userName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserUid) {
        if (currentUserUid != null) {
            firestore.collection("users")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener { doc ->

                    val fullName = doc.getString("fullname")   // ✔️ nome completo REAL
                    val user = doc.getString("username")       // ✔️ nome de usuário

                    userName = when {
                        !fullName.isNullOrBlank() -> fullName.split(" ").first()
                        !user.isNullOrBlank() -> user
                        else -> null
                    }
                }
                .addOnFailureListener {
                    userName = null
                }
        } else {
            userName = null
        }
    }

    var nextEvent by remember { mutableStateOf<Event?>(null) }
    var countdown by remember { mutableStateOf("--") }

    var totalTasks by remember { mutableStateOf(0) }
    var doneTasks by remember { mutableStateOf(0) }

    var tasksEstimatedValue by remember { mutableStateOf(0.0) }
    var tasksDoneValue by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        delay(200)

        val events = if (currentUserUid != null)
            eventDao.getEventsByUser(currentUserUid)
        else emptyList()

        nextEvent = events
            .filter { it.dateTime > System.currentTimeMillis() }
            .minByOrNull { it.dateTime }
    }

    LaunchedEffect(nextEvent) {
        if (nextEvent != null) {
            while (true) {
                countdown = calculateCountdown(nextEvent!!.dateTime)
                delay(1000)
            }
        } else countdown = "--"
    }

    LaunchedEffect(nextEvent) {
        if (nextEvent != null) {
            totalTasks = taskDao.getTasksByEvent(nextEvent!!.id).size
            doneTasks = taskDao.countByStatus(nextEvent!!.id, 2)
            tasksEstimatedValue = taskDao.sumAllValues(nextEvent!!.id)
            tasksDoneValue = taskDao.sumValueByStatus(nextEvent!!.id, 2)
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
                title = "",
                showBackButton = false,
                notificationCount = unread,
                notifications = notifications,
                onNotificationClick = { appState.nav.navigate("notifications") },
                appState = appState
            )
        },
        bottomBar = {
            BottomMenu(
                currentRoute = "home",
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
    ) { paddingValues ->
        HomeContent(
            paddingValues = paddingValues,
            nextEvent = nextEvent,
            countdown = countdown,
            totalTasks = totalTasks,
            doneTasks = doneTasks,
            tasksEstimatedValue = tasksEstimatedValue,
            tasksDoneValue = tasksDoneValue,
            appState = appState,
            navController = navController,
            userName = userName
        )
    }
}

/* -------------------------------------------------------------------------- */

@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    nextEvent: Event?,
    countdown: String,
    totalTasks: Int,
    doneTasks: Int,
    tasksEstimatedValue: Double,
    tasksDoneValue: Double,
    appState: AppState,
    navController: NavController,
    userName: String?
){
    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(DARKBLUE, MEDIUMBLUE),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .background(Color(0xFFF6F8FB))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                HeaderSection(
                    userName = userName,
                    onAvatarClick = { navController.navigate("profile") }
                )

                Spacer(Modifier.height(12.dp))

                SummaryRow(
                    nextEvent = nextEvent,
                    totalTasks = totalTasks,
                    pending = totalTasks - doneTasks,
                    budgetTotal = nextEvent?.budget ?: 0.0,
                    estimatedTasksValue = tasksEstimatedValue,
                    onCardClick = { type ->
                        when (type) {
                            "event" -> nextEvent?.let { navController.navigate("event_details/${it.id}") }
                            "tasks" -> nextEvent?.let { navController.navigate("tasks/${it.id}") }
                            "budget" -> nextEvent?.let { navController.navigate("event_details/${it.id}") }
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                if (nextEvent != null) {
                    NextEventBannerPremium(
                        event = nextEvent,
                        countdown = countdown,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("event_details/${nextEvent.id}") }
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TasksCardSmall(
                            total = totalTasks,
                            done = doneTasks,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                navController.navigate("tasks/${nextEvent.id}")
                            }
                        )

                        BudgetCardSmall(
                            totalBudget = nextEvent.budget,
                            estimatedFromTasks = tasksEstimatedValue,
                            doneValue = tasksDoneValue,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                navController.navigate("event_details/${nextEvent.id}")
                            }
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    AnimatedStatusCard(
                        totalTasks = totalTasks,
                        doneTasks = doneTasks,
                        budgetTotal = nextEvent.budget,
                        estimatedFromTasks = tasksEstimatedValue
                    )

                } else {
                    Spacer(Modifier.height(20.dp))
                    EmptyTopSectionInsane(
                        onCreateEvent = { navController.navigate("create_event") }
                    )
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */

@Composable
fun HeaderSection(
    userName: String?,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            Text(
                if (userName.isNullOrBlank()) "Olá 👋"
                else "Olá, ${userName.capitalize()} 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(Modifier.height(4.dp))
            Text("Resumo do seu próximo evento", color = Color.Gray, fontSize = 13.sp)
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F4FF))
                .pointerInput(Unit) { detectTapGestures { onAvatarClick() } },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MEDIUMBLUE, modifier = Modifier.size(38.dp))
        }
    }
}
/* -------------------------------------------------------------------------- */
/* ------------------------- SUMMARY ROW ------------------------------------ */
/* -------------------------------------------------------------------------- */

@Composable
fun SummaryRow(
    nextEvent: Event?,
    totalTasks: Int,
    pending: Int,
    budgetTotal: Double,
    estimatedTasksValue: Double,
    onCardClick: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            MiniSummaryCard(
                title = "Próximo",
                subtitle = nextEvent?.name ?: "—",
                value = nextEvent?.let { SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it.dateTime)) }
                    ?: "--",
                icon = Icons.Default.CalendarMonth,
                onClick = { onCardClick("event") }
            )
        }
        item {
            MiniSummaryCard(
                title = "Tarefas",
                subtitle = "$totalTasks total",
                value = "$pending pend.",
                icon = Icons.Default.Task,
                badge = if (pending > 0) pending else 0,
                onClick = { onCardClick("tasks") }
            )
        }
        item {
            MiniSummaryCard(
                title = "Orçamento",
                subtitle = "Estimado",
                value = "R$ ${"%.0f".format(estimatedTasksValue)}",
                icon = Icons.Default.CheckCircle,
                onClick = { onCardClick("budget") }
            )
        }
    }
}

@Composable
fun MiniSummaryCard(
    title: String,
    subtitle: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: Int = 0,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 150.dp, height = 100.dp)
            .pointerInput(Unit) { detectTapGestures { onClick() } },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MEDIUMBLUE)
                }

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(title, fontSize = 12.sp, color = Color.Gray)
                    Text(subtitle, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.weight(1f))

                if (badge > 0) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5A5F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$badge", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
    }
}

/* -------------------------------------------------------------------------- */
/* ------------------------- NEXT EVENT CARD -------------------------------- */
/* -------------------------------------------------------------------------- */

@Composable
fun NextEventBannerPremium(event: Event, countdown: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(180.dp)
            .pointerInput(Unit) { detectTapGestures { onClick() } },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Próximo Evento", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(event.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(event.dateTime)))

            Spacer(Modifier.height(16.dp))

            Text("Começa em", color = Color.Gray)
            Text(countdown, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* ---------------------------- TASKS SMALL CARD ---------------------------- */
/* -------------------------------------------------------------------------- */

@Composable
fun TasksCardSmall(total: Int, done: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val pending = total - done

    Card(
        modifier = modifier
            .height(120.dp)
            .pointerInput(Unit) { detectTapGestures { onClick() } },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            CircularProgress(
                progress = if (total == 0) 0f else done.toFloat() / total.toFloat(),
                modifier = Modifier.size(64.dp)
            )

            Spacer(Modifier.width(10.dp))

            Column {
                Text("Tarefas", fontWeight = FontWeight.Bold)
                Text("Concluídas: $done", color = Color.Gray)
                Text("Pendentes: $pending", color = Color.Gray)
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* --------------------------- BUDGET SMALL CARD ---------------------------- */
/* -------------------------------------------------------------------------- */

@Composable
fun BudgetCardSmall(totalBudget: Double, estimatedFromTasks: Double, doneValue: Double, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val progress = if (totalBudget <= 0.0) 0f else (doneValue / totalBudget).toFloat()

    Card(
        modifier = modifier
            .height(120.dp)
            .pointerInput(Unit) { detectTapGestures { onClick() } },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            CircularProgress(progress = progress, modifier = Modifier.size(64.dp))

            Spacer(Modifier.width(10.dp))

            Column {
                Text("Orçamento", fontWeight = FontWeight.Bold)
                Text("Total: R$ ${"%.2f".format(totalBudget)}", color = Color.Gray)
                Text("Estimado: R$ ${"%.2f".format(estimatedFromTasks)}", color = Color.Gray)
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* ---------------------------- STATUS CARD -------------------------------- */
/* -------------------------------------------------------------------------- */

@Composable
fun AnimatedStatusCard(totalTasks: Int, doneTasks: Int, budgetTotal: Double, estimatedFromTasks: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Column(modifier = Modifier.weight(1f)) {
                Text("Status Geral", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text("Tarefas: $doneTasks / $totalTasks concluídas", color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                Text("Orçamento: R$ ${"%.2f".format(estimatedFromTasks)} estimado", color = Color.Gray)
            }

            CircularProgress(
                progress = if (budgetTotal <= 0.0) 0f else (estimatedFromTasks / budgetTotal).toFloat(),
                modifier = Modifier.size(90.dp)
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* ---------------------------- PROGRESS CIRCLE ---------------------------- */
/* -------------------------------------------------------------------------- */

@Composable
fun CircularProgress(progress: Float, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.12f

            drawArc(
                color = Color.LightGray.copy(alpha = 0.25f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
            )

            drawArc(
                color = MEDIUMBLUE,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
            )
        }

        Text("${(animatedProgress * 100).toInt()}%", fontWeight = FontWeight.Bold)
    }
}

/* -------------------------------------------------------------------------- */
/* ---------------------------- EMPTY BANNER -------------------------------- */
/* -------------------------------------------------------------------------- */

@Composable
fun EmptyTopSectionInsane(onCreateEvent: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        // HERO PRINCIPAL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF101425),
                            Color(0xFF192040),
                            Color(0xFF3B82F6)
                        )
                    )
                )
                .pointerInput(Unit) { detectTapGestures { onCreateEvent() } }
        ) {

            // BOLHAS ESTÉTICAS
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color(0xFF60A5FA).copy(alpha = 0.25f),
                    radius = size.minDimension * 0.36f,
                    center = Offset(size.width * 0.30f, size.height * 0.30f)
                )
                drawCircle(
                    color = Color(0xFF2563EB).copy(alpha = 0.22f),
                    radius = size.minDimension * 0.50f,
                    center = Offset(size.width * 0.78f, size.height * 0.75f)
                )
            }

            // GLASS CARD CORRIGIDO
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.82f) // MENOR E MAIS CHIC
                    .height(150.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.20f),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF93C5FD),
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Comece algo incrível ✨",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Crie um evento e desbloqueie todas as ferramentas profissionais do seu app.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Botão mais elegante
                    Button(
                        onClick = onCreateEvent,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            Color(0xFF3B82F6)
                        ),
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth(0.7f)
                    ) {
                        Text("Criar Evento", fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // PLACEHOLDERS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(85.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFDDE6F9),
                                    Color(0xFFF5F8FF)
                                )
                            )
                        )
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* ------------------------------- COUNTDOWN -------------------------------- */
/* -------------------------------------------------------------------------- */

fun calculateCountdown(eventMillis: Long): String {
    val diff = eventMillis - System.currentTimeMillis()
    if (diff <= 0) return "Agora!"
    val days = diff / (1000 * 60 * 60 * 24)
    val hours = (diff / (1000 * 60 * 60)) % 24
    val minutes = (diff / (1000 * 60)) % 60
    val seconds = (diff / 1000) % 60
    return "%02dd %02dh %02dm %02ds".format(days, hours, minutes, seconds)
}
