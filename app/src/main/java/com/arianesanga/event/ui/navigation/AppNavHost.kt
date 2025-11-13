package com.arianesanga.event.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arianesanga.event.ui.screens.*

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("create_event") { CreateEventScreen(navController) }
        composable("event_list") { EventListScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("edit_profile") { EditProfileScreen(navController) } // <--- ADICIONADO
        composable("forgot_password") { ForgotPasswordScreen(navController) }
    }
}