package com.arianesanga.event

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.EventoDatabase
import com.arianesanga.event.data.EventoRepository
import com.arianesanga.event.data.EventoViewModel
import com.arianesanga.event.data.EventoViewModelFactory
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.views.CreateEventActivity
import com.arianesanga.event.views.EventListActivity
import com.arianesanga.event.views.HomeScreenStyled
import com.arianesanga.event.views.ProfileActivity

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // criar db/repository/viewmodel
        val database = EventoDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        val viewModel = ViewModelProvider(
            this,
            EventoViewModelFactory(repository)
        )[EventoViewModel::class.java]

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
