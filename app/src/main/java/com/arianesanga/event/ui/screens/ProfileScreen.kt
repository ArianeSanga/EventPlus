package com.arianesanga.event.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arianesanga.event.R
import com.arianesanga.event.data.local.database.EventDatabase
import com.arianesanga.event.data.local.repository.LocalUserRepository
import com.arianesanga.event.ui.activities.EditProfileActivity
import com.arianesanga.event.ui.activities.EventListActivity
import com.arianesanga.event.ui.activities.HomeActivity
import com.arianesanga.event.ui.activities.ProfileActivity
import com.arianesanga.event.ui.theme.DARKBLUE
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    refreshKey: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = Firebase.auth
    val user = auth.currentUser ?: return
    val uid = user.uid
    val db = FirebaseFirestore.getInstance()
    val localRepo = LocalUserRepository(EventDatabase.getDatabase(context).userDao())
    val scope = rememberCoroutineScope()

    var fullname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Recarregar dados sempre que refreshKey mudar
    LaunchedEffect(refreshKey) {
        localRepo.getUser(uid)?.let { u ->
            fullname = u.fullname
            username = u.username
            phone = u.phone
            photoUri = u.photoUri?.let { Uri.parse(it) }
        }

        db.collection("user").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    fullname = doc.getString("fullname") ?: fullname
                    username = doc.getString("username") ?: username
                    phone = doc.getString("phone") ?: phone
                    photoUri = doc.getString("photoUri")?.let { Uri.parse(it) } ?: photoUri
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "meu perfil",
                showBackButton = true,
                onBack = onBack
            )
        },
        bottomBar = { BottomMenu(context, currentActivity = ProfileActivity::class.java) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FOTO - key força recarregamento
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White)
            ) {
                key(photoUri) {
                    AsyncImage(
                        model = photoUri ?: user.photoUrl ?: R.drawable.account,
                        contentDescription = "Foto",
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // NOME E EMAIL
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(username, fontSize = 22.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                Text(user.email ?: "", fontSize = 16.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(15.dp))

            // CARD DE DADOS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Nome", color = DARKBLUE, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(fullname, fontSize = 18.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(10.dp))

                    Text("Usuário", color = DARKBLUE, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(username, fontSize = 18.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(10.dp))

                    Text("Telefone", color = DARKBLUE, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(phone, fontSize = 18.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(10.dp))

                    Text("E-mail", color = DARKBLUE, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(user.email ?: "", fontSize = 18.sp, color = Color.DarkGray)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onEditProfile,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Editar Perfil", fontSize = 18.sp, color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Sair", fontSize = 18.sp, color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Excluir Conta", fontSize = 18.sp, color = Color.White)
            }

            Spacer(Modifier.height(24.dp))

            // DIALOG DE EXCLUSÃO
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Excluir Conta") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Digite sua senha") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = confirmDelete,
                                    onCheckedChange = { confirmDelete = it }
                                )
                                Text("Estou certo que quero excluir a conta")
                            }
                            if (errorMessage.isNotEmpty()) {
                                Text(errorMessage, color = Color.Red, fontSize = 14.sp)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (!confirmDelete) {
                                errorMessage = "Confirme a exclusão marcando o checkbox."
                                return@TextButton
                            }
                            if (passwordInput.isEmpty()) {
                                errorMessage = "Digite sua senha."
                                return@TextButton
                            }

                            val credential =
                                EmailAuthProvider.getCredential(user.email!!, passwordInput)
                            user.reauthenticate(credential).addOnCompleteListener { authResult ->
                                if (authResult.isSuccessful) {
                                    // Excluir do Room
                                    scope.launch {
                                        localRepo.getUser(uid)?.let { u ->
                                            localRepo.deleteUser(u)
                                        }
                                    }

                                    // Excluir do Firestore
                                    db.collection("user").document(uid).delete()

                                    // Excluir do Firebase Auth
                                    user.delete().addOnCompleteListener { deleteResult ->
                                        if (deleteResult.isSuccessful) {
                                            // Vai para HomeActivity
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    HomeActivity::class.java
                                                )
                                            )
                                            (context as? ComponentActivity)?.finish()
                                        } else {
                                            errorMessage = "Erro ao excluir a conta."
                                        }
                                    }
                                } else {
                                    errorMessage = "Senha incorreta."
                                }
                            }

                        }) {
                            Text("Excluir", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}