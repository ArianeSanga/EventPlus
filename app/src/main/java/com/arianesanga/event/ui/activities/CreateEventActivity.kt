package com.arianesanga.event.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.database.EventoDatabase
import com.arianesanga.event.data.repository.EventoRepository
import com.arianesanga.event.ui.screens.CreateEventScreen
import com.arianesanga.event.viewmodels.EventoViewModel
import com.arianesanga.event.viewmodels.EventoViewModelFactory
import com.arianesanga.event.ui.theme.EventTheme

class CreateEventActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = EventoDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        val factory = EventoViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[EventoViewModel::class.java]

        setContent {
            EventTheme {
                CreateEventScreen(
                    viewModel = viewModel,
                    onFinish = { finish() } // callback para fechar a tela
                )
            }
        }
    }
}