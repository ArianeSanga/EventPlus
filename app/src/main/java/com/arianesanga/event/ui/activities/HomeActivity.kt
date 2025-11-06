package com.arianesanga.event.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arianesanga.event.ui.screens.BottomMenu
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.viewmodels.EventoViewModel
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.database.EventoDatabase
import com.arianesanga.event.data.repository.EventoRepository
import com.arianesanga.event.viewmodels.EventoViewModelFactory

class HomeActivity : ComponentActivity() {
    private lateinit var viewModel: EventoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = EventoDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        viewModel = ViewModelProvider(
            this,
            EventoViewModelFactory(repository)
        )[EventoViewModel::class.java]

        viewModel.baixarEventosFirebase()
        viewModel.carregarEventos()

        setContent {
            EventTheme {
                HomeScreenContent(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(viewModel: EventoViewModel) {
    val context = LocalContext.current
    Scaffold(
        bottomBar = {
            BottomMenu(context, currentActivity = HomeActivity::class.java)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Bem-vindo à Home", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}