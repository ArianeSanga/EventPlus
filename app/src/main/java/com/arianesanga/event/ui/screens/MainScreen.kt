package com.arianesanga.event.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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

// ====================================================================
// 1. COMPOSABLE PRINCIPAL (LOGIN)
// ====================================================================

@Composable
fun Login(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginWithGoogle: () -> Unit,
    onLoginConvidado: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCadastro: () -> Unit
) {
    // Variáveis de estado para controlar a exibição e o tipo de diálogo
    var showDialog by remember { mutableStateOf(false) }
    var dialogIsPrivacyPolicy by remember { mutableStateOf(false) }

    // Função interna para mostrar o diálogo de termos/privacidade
    val onShowTerms: (isPrivacyPolicy: Boolean) -> Unit = { isPrivacyPolicy ->
        dialogIsPrivacyPolicy = isPrivacyPolicy
        showDialog = true
    }

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

        Spacer(modifier = Modifier.weight(1f))

        // --- NOME DO APP (EventPlus) ACIMA DA IMAGEM ---
        Text(
            text = "Eventplus",
            color = WHITE,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp),
            style = LocalTextStyle.current.copy(
                shadow = Shadow(
                    color = LIGHTBLUE,
                    offset = Offset(0f, 0f),
                    blurRadius = 20f
                )
            )
        )

        // --- Logo Principal ---
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Principal",
            modifier = Modifier
                .size(400.dp)
                .padding(bottom = 32.dp),
            contentScale = ContentScale.Fit
        )

        // MENSAGEM DE ERRO
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
            border = BorderStroke(1.dp, WHITE),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WHITE),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.google_icon),
                    contentDescription = "ENTRAR COM GOOGLE",
                    modifier = Modifier.size(20.dp)
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

        Spacer(modifier = Modifier.weight(1f))

        // --- RODAPÉ DOS TERMOS E POLÍTICA (CLICÁVEL) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(
                text = "Ao continuar, você concorda com nossos",
                color = WHITE.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            // Criando links clicáveis com ClickableText
            val annotatedText = buildAnnotatedString {
                // TAG para Termos de Uso
                pushStringAnnotation(tag = "TERMS", annotation = "TERMS")
                withStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold, color = PINK)
                ) {
                    append("Termos de Uso")
                }
                pop()

                append(" e ")

                // TAG para Política de Privacidade
                pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
                withStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold, color = PINK)
                ) {
                    append("Política de Privacidade")
                }
                pop()
            }

            ClickableText(
                text = annotatedText,
                style = LocalTextStyle.current.copy(
                    color = WHITE.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                ),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onShowTerms(false) // Termos de Uso
                        }
                    annotatedText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onShowTerms(true) // Política de Privacidade
                        }
                }
            )
        }
    }

    // ====================================================================
    // 3. LÓGICA DE EXIBIÇÃO DO DIÁLOGO
    // ====================================================================

    if (showDialog) {
        val title = if (dialogIsPrivacyPolicy) "Política de Privacidade" else "Termos de Uso"

        // CONTEÚDO SIMULADO PARA O PROJETO DA FACULDADE (PODE ROLAR)
        val content = if (dialogIsPrivacyPolicy) {
            """
POLÍTICA DE PRIVACIDADE DO EVENTPLUS

1. Informações Coletadas
Coletamos informações que você nos fornece diretamente, como:
a) Informações de Conta: Nome completo, nome de usuário, endereço de e-mail e número de telefone (se fornecido) quando você se cadastra.
b) Dados de Uso: Informações sobre como você usa o aplicativo, incluindo eventos favoritos e pesquisas realizadas.

2. Como Usamos as Informações
Utilizamos as informações coletadas para:
a) Fornecer, manter e melhorar nossos serviços.
b) Personalizar sua experiência, mostrando eventos relevantes.
c) Enviar notificações sobre eventos que você possa ter interesse.

3. Compartilhamento de Informações
Não vendemos ou alugamos suas informações pessoais a terceiros. Podemos compartilhar informações agregadas e não identificáveis publicamente.

4. Segurança de Dados
Implementamos medidas de segurança razoáveis para proteger suas informações contra acesso e divulgação não autorizados. No entanto, nenhum método de transmissão pela Internet é 100% seguro.

5. Seus Direitos
Você pode revisar e atualizar suas informações de perfil a qualquer momento na tela de Perfil. Você também pode solicitar a exclusão de sua conta conforme detalhado em nossos Termos de Uso.

Data de Vigência: 8 de Novembro de 2025
            """.trimIndent()
        } else {
            """
TERMOS DE USO DO EVENTPLUS

1. Aceitação dos Termos
Ao acessar ou usar o aplicativo Eventplus, você concorda em cumprir e se sujeitar a estes Termos de Uso. Se você não concordar com qualquer parte destes termos, não deve usar nosso aplicativo.

2. Uso da Conta
a) Você deve ter pelo menos 13 anos de idade para usar nossos serviços.
b) Você é responsável por manter a confidencialidade de sua senha e por todas as atividades que ocorrem sob sua conta.

3. Conteúdo do Usuário
Você é o único responsável por qualquer conteúdo que você publique ou transmita através do Eventplus. Reservamo-nos o direito de remover qualquer conteúdo que considerarmos ilegal, ofensivo ou que viole estes termos.

4. Limitação de Responsabilidade
O Eventplus é fornecido "como está". Não garantimos que o aplicativo será ininterrupto, seguro ou livre de erros. Em nenhuma circunstância seremos responsáveis por quaisquer danos diretos, indiretos, incidentais ou consequentes resultantes do uso ou da incapacidade de usar o serviço.

5. Alterações nos Termos
Reservamo-nos o direito de modificar ou substituir estes Termos a qualquer momento. Seu uso continuado do aplicativo após quaisquer alterações constitui aceitação dos novos termos.

Data de Vigência: 8 de Novembro de 2025
            """.trimIndent()
        }

        SimpleTextDialog(
            title = title,
            text = content,
            onDismiss = { showDialog = false }
        )
    }
}

// ====================================================================
// 2. FUNÇÃO AUXILIAR PARA O DIÁLOGO (Corrigida e Profissionalizada)
// ====================================================================

@Composable
fun SimpleTextDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        // Alinhamento e estilo do título para parecer mais limpo
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold // Título em negrito
            )
        },
        text = {
            // Usa Column com verticalScroll para permitir que o texto role
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp) // Adiciona um pequeno padding para separar o título
            ) {
                // Conteúdo do corpo com espaçamento nativo
                Text(text)
            }
        },
        confirmButton = {
            // Botão FECHAR/CONFIRMAR com sua cor de destaque PINK
            TextButton(onClick = onDismiss) {
                Text(
                    "LI E CONCORDO",
                    color = PINK,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        // Adiciona um shape arredondado (moderno) ao AlertDialog
        shape = RoundedCornerShape(12.dp)
    )
}