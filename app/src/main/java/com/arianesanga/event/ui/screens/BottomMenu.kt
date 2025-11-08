package com.arianesanga.event.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arianesanga.event.R
import com.arianesanga.event.ui.activities.*
import com.arianesanga.event.ui.theme.BLACK
import com.arianesanga.event.ui.theme.PINK

data class BottomMenuItem(
    val icon: Painter,
    val targetActivity: Class<*>,
    val iconSizeDp: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomMenu(context: Context, currentActivity: Class<*>) {
    // Aqui definimos a altura da barra apenas uma vez na tela
    val alturaBarra = 70

    BottomMenu(context, currentActivity, alturaBarra)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomMenu(
    context: Context,
    currentActivity: Class<*>,
    barHeightDp: Int
) {
    var selectedIndex by remember { mutableStateOf(0) }

    val menuItems = listOf(
        BottomMenuItem(painterResource(R.drawable.home), HomeActivity::class.java, 35),
        BottomMenuItem(painterResource(R.drawable.add), CreateEventActivity::class.java, 40),
        BottomMenuItem(painterResource(R.drawable.list), EventListActivity::class.java, 35),
        BottomMenuItem(painterResource(R.drawable.account), ProfileActivity::class.java, 35)
    )

    // Define o índice do item selecionado
    menuItems.forEachIndexed { index, item ->
        if (item.targetActivity == currentActivity) selectedIndex = index
    }

    Box(modifier = Modifier.height(barHeightDp.dp)) {
        NavigationBar(
            containerColor = PINK,
            modifier = Modifier.fillMaxWidth()
        ) {
            menuItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = {
                        if (item.targetActivity != currentActivity) {
                            context.startActivity(Intent(context, item.targetActivity))
                        }
                    },
                    icon = {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Icon(
                                painter = item.icon,
                                contentDescription = null,
                                tint = if (selectedIndex == index) BLACK else Color.White,
                                modifier = Modifier.size(item.iconSizeDp.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(if (selectedIndex == index) Color.Black else Color.Transparent)
                            )
                        }
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BLACK,
                        unselectedIconColor = Color.LightGray,
                        indicatorColor = PINK
                    )
                )
            }
        }
    }
}