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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arianesanga.event.R
import com.arianesanga.event.ui.theme.MEDIUMBLUE
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.LIGHTBLUE
import com.arianesanga.event.ui.theme.PINK
import com.arianesanga.event.ui.theme.WHITE

@Composable
fun Login(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginWithGoogle: () -> Unit,
    onLoginConvidado: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCadastro: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(DARKBLUE, MEDIUMBLUE, DARKBLUE, DARKBLUE)
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
            text = "Eventplus",
            color = WHITE,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp),
            style = LocalTextStyle.current.copy(
                shadow = Shadow(
                    color = LIGHTBLUE,        // cor da sombra (brilho)
                    offset = Offset(0f, 0f),  // deslocamento da sombra
                    blurRadius = 20f          // intensidade do brilho
                )
            )
        )

        // --- Logo Principal (Movida para o Meio) ---
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Principal",
            modifier = Modifier
                .size(400.dp)
                .padding(bottom = 32.dp), // Espaçamento abaixo da imagem
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
            onClick = onNavigateToLogin,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = PINK),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "ENTRAR COM E-MAIL",
                    tint = WHITE,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENTRAR COM E-MAIL",
                    fontSize = 16.sp,
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
                Image(
                    painter = painterResource(id = R.drawable.google_icon),
                    contentDescription = "ENTRAR COM GOOGLE",
                    modifier = Modifier.size(20.dp) // tamanho do ícone
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENTRAR COM GOOGLE",
                    fontSize = 16.sp,
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
                color = PINK,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
                .clickable {
                    // Ação ao clicar nos termos (opcional)
                }
        ) {
            // Linha 1
            Text(
                text = "Ao continuar, você concorda com nossos",
                color = WHITE.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            // Linha 2
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = PINK
                        )
                    ) {
                        append("Termos de Uso")
                    }
                    append(" e ")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = PINK
                        )
                    ) {
                        append("Política de Privacidade")
                    }
                },
                color = WHITE.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}