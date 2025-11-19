package com.arianesanga.event.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.arianesanga.event.data.local.database.AppDatabase
import com.arianesanga.event.data.repository.NotificationRepository

class AppState(
    val nav: NavController,
    val notificationRepo: NotificationRepository
) {
    val unreadCount = notificationRepo.unreadCountFlow()
}

@Composable
fun rememberAppState(nav: NavController): AppState {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { NotificationRepository(db.notificationDao()) }

    return remember(nav) { AppState(nav, repo) }
}