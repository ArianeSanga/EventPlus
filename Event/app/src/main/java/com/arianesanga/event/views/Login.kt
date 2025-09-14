package com.arianesanga.event.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arianesanga.event.R
import com.arianesanga.event.ui.theme.BROWN900
import com.arianesanga.event.ui.theme.GRAY900
import com.arianesanga.event.ui.theme.ORANGE
import com.arianesanga.event.ui.theme.WHITE

@Composable
fun Login(onLoginWithGoogle: () -> Unit) {
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
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .width(400.dp)
                .height(420.dp),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        Text(
            text = buildAnnotatedString {
                append("Seja Bem-vindo! Prepare-se para organizar os")
                withStyle(
                    style = SpanStyle(
                        color = ORANGE
                    )
                ) {
                    append(" melhores \neventos ")
                }
                append("junto com seus amigos")
            },
            color = WHITE,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )

        Text(
            text = "Com um simples clique, você pode criar todo um evento e chamar a galera ",
            color = WHITE,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 20.dp, 20.dp, 30.dp)
        )

        // Botão removido do Row e adicionado diretamente na Column
        Button(
            onClick = { onLoginWithGoogle() },
            colors = ButtonDefaults.buttonColors(
                containerColor = ORANGE
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(horizontal = 20.dp) // Adicionado padding para centralizar o botão
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(50),
                    spotColor = ORANGE
                )
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text(
                text = "Entrar com Google",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WHITE
            )
        }
    } // Fecha o Column
} // Fecha o Login