package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arianesanga.event.ui.theme.EventTheme

class ConvidadoLoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recebe o eventoId que será passado quando o convidado for entrar
        val eventoId = intent.getIntExtra("eventoId", -1)

        setContent {
            EventTheme {
                ConvidadoLoginScreen(eventoId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvidadoLoginScreen(eventoId: Int) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") } // Opcional, pode deixar vazio
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Entrar como Convidado", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email do Convidado") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotEmpty()) {
                    if (eventoId > 0) {
                        // Passa para a tela do convidado com o evento específico
                        val intent = Intent(context, ConvidadoHomeActivity::class.java)
                        intent.putExtra("eventoId", eventoId)
                        intent.putExtra("convidadoEmail", email)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Evento inválido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Preencha o email do convidado", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }
    }
}
