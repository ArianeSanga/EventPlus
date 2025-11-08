package com.arianesanga.event.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arianesanga.event.R
import com.arianesanga.event.ui.theme.DARKBLUE
import com.arianesanga.event.ui.theme.PINK

@Composable
fun TopAppBar(
    title: String,
    showBackButton: Boolean = false,
    onBack: (() -> Unit)? = null,
    fontSize: Int = 20,
    fontWeight: FontWeight = FontWeight.ExtraBold
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(PINK)
            .padding(horizontal = 8.dp)
    ) {
        if (showBackButton && onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIos,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-1).dp)
        )
    }
}