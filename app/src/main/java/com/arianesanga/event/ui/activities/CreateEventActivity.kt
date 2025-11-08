package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.arianesanga.event.data.local.database.EventDatabase
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.data.local.repository.LocalEventRepository
import com.arianesanga.event.data.remote.firebase.FirebaseAuthService
import com.arianesanga.event.data.remote.repository.RemoteEventRepository
import com.arianesanga.event.ui.screens.CreateEventScreen
import com.arianesanga.event.ui.theme.EventTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class CreateEventActivity : ComponentActivity() {

    private var isSaving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = EventDatabase.getDatabase(applicationContext)
        val localRepo = LocalEventRepository(db.eventDao())
        val remoteRepo = RemoteEventRepository()
        val authService = FirebaseAuthService()

        setContent {
            EventTheme {
                CreateEventScreen(
                    onSave = { name, description, date, location, budget, imageUri ->
                        if (isSaving) return@CreateEventScreen
                        isSaving = true

                        val userUid = authService.getCurrentUser()?.uid
                        if (userUid == null) {
                            println("⚠️ Usuário não autenticado.")
                            isSaving = false
                            return@CreateEventScreen
                        }

                        val id = UUID.randomUUID().toString()
                        val event = Event(
                            id = id,
                            userUid = userUid,
                            name = name,
                            description = description,
                            date = date,
                            location = location,
                            budget = budget,
                            imageUri = imageUri
                        )

                        lifecycleScope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    localRepo.insertOrUpdateEvent(event)
                                }

                                val data = mapOf(
                                    "id" to id,
                                    "userUid" to userUid,
                                    "name" to name,
                                    "description" to description,
                                    "date" to date,
                                    "location" to location,
                                    "budget" to budget,
                                    "imageUri" to (imageUri ?: "")
                                )

                                remoteRepo.createOrUpdateEvent(userUid, id, data) { success, msg ->
                                    lifecycleScope.launch {
                                        if (success) {
                                            startActivity(
                                                Intent(
                                                    this@CreateEventActivity,
                                                    EventListActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else {
                                            println("❌ Erro ao salvar no Firebase: $msg")
                                            isSaving = false
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                println("❌ Erro ao salvar evento: ${e.message}")
                                isSaving = false
                            }
                        }
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}