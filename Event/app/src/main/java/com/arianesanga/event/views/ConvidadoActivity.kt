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
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.*
import com.arianesanga.event.ui.theme.EventTheme
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class ConvidadoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventoId = intent.getIntExtra("eventoId", -1)
        if (eventoId == -1) {
            Toast.makeText(this, "Evento inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val database = EventoDatabase.getDatabase(this)
        val repository = ConvidadoRepository(database.convidadoDao())
        val viewModel = ViewModelProvider(
            this,
            ConvidadoViewModelFactory(repository)
        )[ConvidadoViewModel::class.java]

        // Carrega convidados locais e observa Firebase
        viewModel.carregarConvidados(eventoId)
        viewModel.observarConvidadosFirebase(eventoId)

        setContent {
            EventTheme {
                ConvidadoScreen(viewModel, eventoId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvidadoScreen(viewModel: ConvidadoViewModel, eventoId: Int) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val convidados by viewModel.convidados.collectAsState()
    val context = LocalContext.current

    val senhaPadrao = "123456" // pode gerar aleatória depois

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Convidados do Evento") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Campos de entrada
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefone,
                onValueChange = { telefone = it },
                label = { Text("Telefone") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botão de adicionar convidado
            Button(
                onClick = {
                    if (nome.isNotEmpty() && telefone.isNotEmpty() && email.isNotEmpty()) {
                        val convidado = Convidado(
                            nome = nome,
                            telefone = telefone,
                            eventoId = eventoId,
                            email = email,
                        )

                        viewModel.criarContaEAdicionarConvidado(convidado, senhaPadrao)

                        Toast.makeText(
                            context,
                            "Convidado adicionado!\nEmail: $email\nSenha: $senhaPadrao",
                            Toast.LENGTH_LONG
                        ).show()

                        // Limpa campos
                        nome = ""
                        telefone = ""
                        email = ""
                    } else {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar Convidado")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de convidados
            LazyColumn {
                items(convidados) { convidado ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nome: ${convidado.nome}")
                            Text("Telefone: ${convidado.telefone}")
                            Text("Email: ${convidado.email}")
                        }
                    }
                }
            }
        }
    }
}
