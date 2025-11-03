package com.arianesanga.event.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.database.EventoDatabase
import com.arianesanga.event.data.model.Tarefa
import com.arianesanga.event.data.repository.TarefaRepository
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.viewmodels.TarefaViewModel
import com.arianesanga.event.viewmodels.TarefaViewModelFactory

class TarefaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventoId = intent.getIntExtra("eventoId", -1)
        if (eventoId == -1) {
            Toast.makeText(this, "Evento inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val database = EventoDatabase.getDatabase(this)
        val repository = TarefaRepository(database.tarefaDao())
        val viewModel = ViewModelProvider(
            this,
            TarefaViewModelFactory(repository)
        )[TarefaViewModel::class.java]

        // Carrega as tarefas relacionadas ao eventoId
        viewModel.carregarTarefas(eventoId)

        setContent {
            EventTheme {
                TarefaScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarefaScreen(viewModel: TarefaViewModel) {
    // Variáveis de estado para o formulário de nova tarefa
    var descricao by remember { mutableStateOf("") }
    var dataLimite by remember { mutableStateOf("") } // Implementação simples da data

    val tarefas by viewModel.tarefas.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Tarefas do Evento") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Formulário de Nova Tarefa
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Nova Tarefa") },
                    modifier = Modifier.weight(1f)
                )

                // Simulação simples de data (Poderíamos usar um DatePicker, mas vamos manter simples por enquanto)
                OutlinedTextField(
                    value = dataLimite,
                    onValueChange = { dataLimite = it },
                    label = { Text("Data (dd/MM)") },
                    modifier = Modifier.width(100.dp)
                )

                Button(
                    onClick = {
                        if (descricao.isNotBlank()) {
                            viewModel.adicionarTarefa(descricao, dataLimite)
                            descricao = "" // Limpa o campo
                            dataLimite = ""
                            Toast.makeText(context, "Tarefa adicionada!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Tarefa")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Lista de Tarefas
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tarefas, key = { it.id }) { tarefa ->
                    TarefaItem(
                        tarefa = tarefa,
                        onToggleDone = viewModel::alternarConclusao,
                        onDelete = viewModel::deletarTarefa
                    )
                }
            }
        }
    }
}

@Composable
fun TarefaItem(tarefa: Tarefa, onToggleDone: (Tarefa) -> Unit, onDelete: (Tarefa) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = tarefa.concluida,
                    onCheckedChange = { onToggleDone(tarefa) }
                )
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = tarefa.descricao,
                        style = MaterialTheme.typography.bodyLarge,
                        // Adicionar um estilo para riscar o texto se concluída
                        color = if (tarefa.concluida) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    tarefa.dataLimite?.takeIf { it.isNotBlank() }?.let { data ->
                        Text(
                            text = "Prazo: $data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error // Destaque para o prazo
                        )
                    }
                }
            }
            IconButton(onClick = { onDelete(tarefa) }) {
                Icon(Icons.Default.Delete, contentDescription = "Deletar Tarefa")
            }
        }
    }
}