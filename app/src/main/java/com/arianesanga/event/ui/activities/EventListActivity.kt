package com.arianesanga.event.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.arianesanga.event.data.local.database.EventDatabase
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.data.local.repository.LocalEventRepository
import com.arianesanga.event.data.remote.firebase.FirebaseAuthService
import com.arianesanga.event.data.remote.repository.RemoteEventRepository
import com.arianesanga.event.ui.screens.EventListScreen
import com.arianesanga.event.ui.theme.EventTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = EventDatabase.getDatabase(applicationContext)
        val localRepo = LocalEventRepository(db.eventDao())
        val remoteRepo = RemoteEventRepository()
        val authService = FirebaseAuthService()
        val userUid = authService.getCurrentUser()?.uid

        setContent {
            EventTheme {
                EventListScreen(
                    userUid = userUid,
                    localRepo = localRepo,
                    remoteRepo = remoteRepo,
                    onBack = { finish() }
                )
            }
        }

        // 🔹 Sincroniza dados do Firebase com o banco local
        if (userUid != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                remoteRepo.getEventsByUser(userUid) { events ->
                    if (events != null) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            localRepo.getEventsByUser(userUid).forEach { localRepo.deleteEvent(it) }
                            events.forEach { data ->
                                val event = Event(
                                    id = data["id"] as String,
                                    userUid = data["userUid"] as String,
                                    name = data["name"] as String,
                                    description = data["description"] as String,
                                    date = data["date"] as String,
                                    location = data["location"] as String,
                                    budget = (data["budget"] as? Number)?.toDouble() ?: 0.0,
                                    imageUri = data["imageUri"] as? String
                                )
                                localRepo.insertOrUpdateEvent(event)
                            }
                        }
                    }
                }
            }
        }
    }
}