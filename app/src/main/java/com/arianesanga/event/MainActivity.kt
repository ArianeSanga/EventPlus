package com.arianesanga.event

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.arianesanga.event.ui.navigation.AppNavHost
import com.arianesanga.event.ui.theme.EventTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EventTheme(darkTheme = true, dynamicColor = false)  {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}