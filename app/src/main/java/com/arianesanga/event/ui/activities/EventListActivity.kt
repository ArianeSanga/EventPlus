package com.arianesanga.event.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.database.EventoDatabase
import com.arianesanga.event.data.repository.EventoRepository
import com.arianesanga.event.ui.screens.EventListScreen
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.viewmodels.EventoViewModel
import com.arianesanga.event.viewmodels.EventoViewModelFactory

class EventListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = EventoDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        val viewModel = ViewModelProvider(
            this,
            EventoViewModelFactory(repository)
        )[EventoViewModel::class.java]

        // Lógica da Activity (carrega e sincroniza eventos)
        viewModel.carregarEventos()
        viewModel.sincronizarEventosFirebase()

        setContent {
            EventTheme {
                EventListScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}