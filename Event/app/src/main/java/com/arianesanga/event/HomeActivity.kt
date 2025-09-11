package com.arianesanga.event

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.views.CreateEventActivity
import com.arianesanga.event.views.HomeScreenStyled

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventTheme {
                HomeScreenStyled(
                    onCreateEventClick = {
                        startActivity(Intent(this, CreateEventActivity::class.java))
                    },
                    onViewEventsClick = { /* TODO: Navegar para lista de eventos */ },
                    onProfileClick = { /* TODO: Navegar para perfil */ }
                )
            }
        }
    }
}
