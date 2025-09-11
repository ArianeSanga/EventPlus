package com.arianesanga.event

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.views.HomeScreen

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventTheme {
                HomeScreen() // mostra a tela principal
            }
        }
    }
}
