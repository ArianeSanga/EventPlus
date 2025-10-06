package com.arianesanga.event.views

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arianesanga.event.ui.theme.EventTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ConvidadoHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = intent.getStringExtra("convidadoEmail") // email do convidado

        setContent {
            EventTheme {
                // Chama o Composable diretamente aqui
                ConvidadoHomeScreen(email)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvidadoHomeScreen(userEmail: String?) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var isLoading by remember { mutableStateOf(true) }
    var convites by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    // Pair<EventoNome, ConvidadoNome>

    LaunchedEffect(userEmail) {
        if (!userEmail.isNullOrEmpty()) {
            try {
                val snapshot = db.collection("convidados")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .await()

                val listaConvites = mutableListOf<Pair<String, String>>()

                for (doc in snapshot.documents) {
                    val nomeConvidado = doc.getString("nome") ?: "Sem Nome"
                    val eventoId = doc.getLong("eventoId")?.toInt() ?: continue

                    val eventoSnapshot = db.collection("eventos")
                        .document(eventoId.toString())
                        .get()
                        .await()

                    val nomeEvento = eventoSnapshot.getString("nome") ?: "Evento Sem Nome"

                    listaConvites.add(nomeEvento to nomeConvidado)
                }

                convites = listaConvites
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao buscar convites: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        } else {
            Toast.makeText(context, "Email inválido", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Meus Convites") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Carregando convites...")
                }
                convites.isEmpty() -> {
                    Text("Você não tem convites.")
                }
                else -> {
                    LazyColumn {
                        items(convites) { (nomeEvento, nomeConvidado) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Evento: $nomeEvento", style = MaterialTheme.typography.titleMedium)
                                    Text("Convidado: $nomeConvidado")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
