package com.arianesanga.event.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arianesanga.event.R
import com.arianesanga.event.ui.theme.BLACK
import com.arianesanga.event.ui.theme.PINK

data class BottomMenuItem(
    val icon: Painter,
    val route: String,
    val iconSizeDp: Int
)

@Composable
fun BottomMenu(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val menuItems = listOf(
        BottomMenuItem(painterResource(R.drawable.home), "home", 35),
        BottomMenuItem(painterResource(R.drawable.add), "create_event", 40),
        BottomMenuItem(painterResource(R.drawable.list), "event_list", 35),
        BottomMenuItem(painterResource(R.drawable.account), "profile", 35)
    )

    val selectedIndex = menuItems.indexOfFirst { it.route == currentRoute }

    NavigationBar(containerColor = PINK) {
        menuItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { if (item.route != currentRoute) onNavigate(item.route) },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                .clip(CircleShape)
                                .background(if (selectedIndex == index) BLACK else Color.Transparent)
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BLACK,
                    unselectedIconColor = Color.White,
                    indicatorColor = PINK
                )
            )
        }
    }
}