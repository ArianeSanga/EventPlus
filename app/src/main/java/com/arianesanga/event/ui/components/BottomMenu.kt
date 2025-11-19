package com.arianesanga.event.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arianesanga.event.R
import com.arianesanga.event.ui.theme.MEDIUMBLUE

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
        BottomMenuItem(painterResource(R.drawable.home), "home", 32),
        BottomMenuItem(painterResource(R.drawable.list), "event_list", 28),
        BottomMenuItem(painterResource(R.drawable.account), "profile", 26)
    )

    val selectedIndex = menuItems.indexOfFirst { it.route == currentRoute }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 25.dp)
            .shadow(15.dp, RoundedCornerShape(30.dp), clip = false)
            .clip(RoundedCornerShape(25.dp))
            .background(Color.White)
            .height(70.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            menuItems.forEachIndexed { index, item ->

                Surface(
                    onClick = {
                        if (item.route != currentRoute) onNavigate(item.route)
                    },
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 11.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = item.icon,
                                contentDescription = null,
                                tint = if (selectedIndex == index) MEDIUMBLUE else Color.Gray,
                                modifier = Modifier.size(item.iconSizeDp.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIndex == index) MEDIUMBLUE else Color.Transparent
                                )
                        )
                    }
                }
            }
        }
    }
}