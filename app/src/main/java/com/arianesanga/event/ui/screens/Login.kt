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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
    onNavigateToEmailLogin: () -> Unit,
    onNavigateToCadastro: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GRAY900, BROWN900, GRAY900, GRAY900)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 40.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // *****************
        // 1. SPACER DE PESO 1f: Empurra o bloco de conteúdo para o meio da tela.
        // *****************
        Spacer(modifier = Modifier.weight(1f))

        // --- NOVO: NOME DO APP (EventPlus) ACIMA DA IMAGEM ---
        Text(
            text = "EventPlus",
            color = WHITE, // Cor branca para contraste com o fundo escuro
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp) // Espaçamento entre o nome e a logo
        )

        // --- Logo Principal (Movida para o Meio) ---
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Principal",
            modifier = Modifier
                .size(300.dp)
                .padding(bottom = 60.dp), // Espaçamento abaixo da imagem
            contentScale = ContentScale.Fit
        )

        // MENSAGEM DE ERRO (Google Login)
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // BOTÃO 1: ENTRAR COM E-MAIL
        Button(
            onClick = onNavigateToEmailLogin,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = ORANGE),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Entrar com E-mail",
                    tint = WHITE,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Entrar com E-mail",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WHITE
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÃO 2: ENTRAR COM GOOGLE
        OutlinedButton(
            onClick = { onLoginWithGoogle() },
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, WHITE), // Borda Branca para visibilidade
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WHITE), // Texto/Ícone Branco
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Entrar com Google",
                    tint = WHITE,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Entrar com Google",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WHITE
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- LINK DE CADASTRO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Não tem uma conta? ",
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

        // *****************
        // 2. SPACER DE PESO 1f: Empurra o rodapé para o final da tela, mantendo a centralização.
        // *****************
        Spacer(modifier = Modifier.weight(1f))

        // --- TERMOS E CONDIÇÕES (Rodapé) ---
        val annotatedString = buildAnnotatedString {
            append("Ao continuar, você concorda com nossos ")
            withStyle(style = SpanStyle(
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold,
                color = ORANGE
            )
            ) {
                append("Termos de Uso")
            }
            append(" e ")
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold, color = ORANGE)) {
                append("Política de Privacidade")
            }
            append(".")
        }

        Text(
            text = annotatedString,
            color = WHITE.copy(alpha = 0.7f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable {
                    // Ação ao clicar nos termos
                }
        )
    }
}