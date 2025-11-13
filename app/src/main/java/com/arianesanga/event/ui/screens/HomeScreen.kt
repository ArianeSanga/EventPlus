package com.arianesanga.event.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arianesanga.event.R
import com.arianesanga.event.ui.components.BottomMenu
import com.arianesanga.event.ui.components.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = "início",
                showBackButton = false
            )
        },
        bottomBar = {
            BottomMenu(
                currentRoute = "home",
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 Logo (opcional)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo do App",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Bem-vindo(a) ao EventApp 🎉",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Gerencie seus eventos, acompanhe suas atividades e aproveite ao máximo sua experiência!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}