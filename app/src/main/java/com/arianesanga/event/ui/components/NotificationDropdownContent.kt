package com.arianesanga.event.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arianesanga.event.data.local.model.NotificationEntity

@Composable
fun NotificationDropdownContent(
    notifications: List<NotificationEntity>,
    onNotificationClick: (Long?) -> Unit
) {

    if (notifications.isEmpty()) {
        Text(
            "Nenhuma notificação",
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = Color.Gray
        )
        return
    }

    Column(
        modifier = Modifier.padding(vertical = 6.dp)
    ) {

        notifications.take(5).forEach { n ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = n.title,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1B1F)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = n.message,
                    fontSize = 13.sp,
                    color = Color(0xFF777777)
                )
            }
        }
    }
}