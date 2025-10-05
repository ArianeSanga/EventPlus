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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConvidadoHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventTheme {
                // Passamos o email do usuário para garantir que o Composable só funcione se estiver logado
                ConvidadoHomeScreen(FirebaseAuth.getInstance().currentUser?.email)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Recebe o email como parâmetro para garantir que o estado de login seja considerado
fun ConvidadoHomeScreen(userEmail: String?) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Usamos um StateFlow ou State para gerenciar o estado da busca
    var eventosIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userEmail) {
        if (userEmail != null) {
            isLoading = true
            db.collection("convidados")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { docs ->
                    // Mapeia os IDs dos eventos que este convidado faz parte
                    eventosIds = docs.mapNotNull { it.getLong("eventoId")?.toString() }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao buscar convites: ${e.message}", Toast.LENGTH_LONG).show()
                    eventosIds = emptyList()
                }
                .addOnCompleteListener {
                    isLoading = false
                }
        } else {
            // Se o email for nulo, não está logado ou a informação não está disponível
            isLoading = false
            Toast.makeText(context, "Usuário não logado ou email indisponível.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Meus Convites") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            when {
                isLoading -> {
                    // Estado de Carregamento
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Buscando seus convites...")
                }
                eventosIds.isEmpty() -> {
                    // Estado de Vazio
                    Text("Você não tem convites ainda.")
                }
                else -> {
                    // Estado de Sucesso
                    LazyColumn {
                        items(eventosIds) { eventoId ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Evento ID: $eventoId", style = MaterialTheme.typography.titleMedium)
                                    // REQUER PRÓXIMO PASSO: Buscar nome real do evento no Room
                                    Text("Depois podemos buscar mais detalhes no banco local (Room)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}