package com.arianesanga.event.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arianesanga.event.R
import com.arianesanga.event.ui.theme.BROWN900
import com.arianesanga.event.ui.theme.GRAY900
import com.arianesanga.event.ui.theme.ORANGE
import com.arianesanga.event.ui.theme.WHITE

@Composable
fun Login(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginWithGoogle: () -> Unit,
    onLoginConvidado: () -> Unit,
    onLoginWithEmailPassword: (email: String, password: String) -> Unit,
    onNavigateToCadastro: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        GRAY900,
                        BROWN900,
                        GRAY900,
                        GRAY900
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            // Adiciona padding geral para não colar nas bordas
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        // Logo sutil no topo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo do Event App",
            modifier = Modifier
                .size(100.dp), // Tamanho bem menor e mais profissional
            contentScale = ContentScale.Fit // Fit é melhor para logos
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Título principal
        Text(
            text = "Bem-vindo de Volta!",
            color = WHITE,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        // Subtítulo
        Text(
            text = "Acesse para organizar seus eventos",
            color = WHITE.copy(alpha = 0.8f), // Cor mais suave
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // CAMPO EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            textStyle = LocalTextStyle.current.copy(color = WHITE),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), // Canto mais suave
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ORANGE,
                unfocusedBorderColor = ORANGE,
                focusedLabelColor = ORANGE,
                unfocusedLabelColor = ORANGE,
                cursorColor = ORANGE
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp)) // Espaçamento consistente

        // CAMPO SENHA
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            textStyle = LocalTextStyle.current.copy(color = WHITE),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), // Canto mais suave
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ORANGE,
                unfocusedBorderColor = ORANGE,
                focusedLabelColor = ORANGE,
                unfocusedLabelColor = ORANGE,
                cursorColor = ORANGE
            ),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.VisibilityOff
                else Icons.Filled.Visibility

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, "Mostrar/Ocultar senha", tint = ORANGE) // Cor do ícone
                }
            }
        )

        // MENSAGEM DE ERRO
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // Mais espaço antes do botão

        // BOTÃO DE LOGIN (Ação Primária)
        Button(
            onClick = { onLoginWithEmailPassword(email, password) },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = ORANGE
            ),
            shape = RoundedCornerShape(12.dp), // Canto mais suave
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
            // Sombra removida para um look mais limpo (Button já tem elevação padrão)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = WHITE,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = "Entrar com E-mail",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WHITE
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÃO DE LOGIN COM GOOGLE (Ação Secundária)
        OutlinedButton( // Alterado para OutlinedButton para diferenciar
            onClick = { onLoginWithGoogle() },
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ORANGE), // Borda com a cor principal
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            // Ícone do Google (Idealmente, você adicionaria o drawable do Google aqui)
            // Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(22.dp))
            // Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Entrar com Google",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WHITE // Texto branco
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- LINK DE CADASTRO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ainda não tem conta? ",
                color = WHITE.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
            Text(
                text = "Cadastre-se",
                color = ORANGE,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(
                        enabled = !isLoading,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateToCadastro
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))



        Spacer(modifier = Modifier.height(32.dp))
    }
}