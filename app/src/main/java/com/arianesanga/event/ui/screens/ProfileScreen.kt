package com.arianesanga.event.ui.screens

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arianesanga.event.R
import com.arianesanga.event.ui.activities.EditProfileActivity
import com.arianesanga.event.ui.activities.ProfileActivity
import com.arianesanga.event.ui.theme.YELLOW
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val user = Firebase.auth.currentUser
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var fullName by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf<String?>(null) }

    // 🔍 Buscar dados do Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        fullName = document.getString("fullName")
                        username = document.getString("username")
                        phone = document.getString("phone")
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu perfil") },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomMenu(context, currentActivity = ProfileActivity::class.java)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 📸 Foto do usuário (ou ícone padrão)
            AsyncImage(
                model = user?.photoUrl ?: R.drawable.account,
                contentDescription = "Foto do usuário",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🧑 Nome de usuário (em negrito)
            Text(
                text = username ?: user?.displayName ?: "Usuário",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // ✉️ E-mail
            Text(
                text = user?.email ?: "E-mail não disponível",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 📋 Informações do perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileField(label = "Nome completo", value = fullName ?: "Carregando...")
                    ProfileField(label = "Telefone", value = phone ?: "Carregando...")
                    ProfileField(label = "E-mail", value = user?.email ?: "Carregando...")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 🟡 Botão "Editar perfil"
            Button(
                onClick = {
                    val intent = Intent(context, EditProfileActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text("Editar perfil", color = MaterialTheme.colorScheme.onPrimary)
            }

            // 🔴 Botão "Sair da conta"
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = YELLOW
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sair da conta", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}