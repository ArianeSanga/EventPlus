package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.database.EventoDatabase
import com.arianesanga.event.data.repository.EventoRepository
import com.arianesanga.event.viewmodels.EventoViewModel
import com.arianesanga.event.viewmodels.EventoViewModelFactory
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.ui.screens.HomeScreenStyled

// import com.arianesanga.event.ui.screens.OrganizerScreens // Se não for usado, remova este import.

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = EventoDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        val viewModel = ViewModelProvider(
            this,
            EventoViewModelFactory(repository)
        )[EventoViewModel::class.java]


        viewModel.baixarEventosFirebase()
        viewModel.carregarEventos()


        setContent {
            EventTheme {
                HomeScreenStyled(
                    viewModel = viewModel,
                    onCreateEventClick = {
                        startActivity(Intent(this@HomeActivity, CreateEventActivity::class.java))
                    },
                    onViewEventsClick = {
                        startActivity(Intent(this@HomeActivity, EventListActivity::class.java))
                    },
                    onProfileClick = {
                        startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
                    }
                )
            }
        }
    }
}