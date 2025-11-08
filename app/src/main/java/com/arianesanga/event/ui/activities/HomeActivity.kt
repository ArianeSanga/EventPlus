package com.arianesanga.event.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arianesanga.event.ui.screens.HomeScreen
import com.arianesanga.event.ui.theme.EventTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EventTheme {
                HomeScreen()
            }
        }
    }
}