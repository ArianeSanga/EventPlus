package com.arianesanga.event.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arianesanga.event.ui.components.rememberAppState
import com.arianesanga.event.ui.screens.CreateEventScreen
import com.arianesanga.event.ui.screens.CreateTaskScreen
import com.arianesanga.event.ui.screens.EditEventScreen
import com.arianesanga.event.ui.screens.EditProfileScreen
import com.arianesanga.event.ui.screens.EditTaskScreen
import com.arianesanga.event.ui.screens.EventDetailsScreen
import com.arianesanga.event.ui.screens.EventListScreen
import com.arianesanga.event.ui.screens.ForgotPasswordScreen
import com.arianesanga.event.ui.screens.HomeScreen
import com.arianesanga.event.ui.screens.LoginScreen
import com.arianesanga.event.ui.screens.MainScreen
import com.arianesanga.event.ui.screens.ProfileScreen
import com.arianesanga.event.ui.screens.RegisterScreen
import com.arianesanga.event.ui.screens.TaskListScreen

@Composable
fun AppNavHost(navController: NavHostController) {

    val appState = rememberAppState(navController)

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }

        composable("home") {
            HomeScreen(
                navController,
                appState = appState
            )
        }

        composable("create_event") {
            CreateEventScreen(
                navController,
                appState = appState
            )
        }
        composable("event_list") {
            EventListScreen(
                navController,
                appState = appState
            )
        }
        composable("profile") {
            ProfileScreen(
                navController,
                appState = appState
            )
        }

        composable("edit_profile") {
            EditProfileScreen(
                navController,
                appState = appState
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                navController
            )
        }

        composable("edit_event/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")?.toIntOrNull()
            if (id != null)
                EditEventScreen(
                    navController,
                    id,
                    appState = appState
                )
        }
        composable("event_details/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")?.toIntOrNull()
            if (id != null) EventDetailsScreen(
                navController,
                id,
                appState = appState
            )
        }
        composable("tasks/{eventId}") { backStack ->
            val eventId = backStack.arguments?.getString("eventId")?.toIntOrNull()
            if (eventId != null) {
                TaskListScreen(
                    navController,
                    eventId,
                    appState = appState
                )
            }
        }
        composable("create_task/{eventId}") { backStack ->
            val eventId = backStack.arguments?.getString("eventId")?.toIntOrNull()
            if (eventId != null) {
                CreateTaskScreen(
                    navController,
                    eventId,
                    appState = appState
                )
            }
        }
        composable("edit_task/{taskId}") { backStack ->
            val taskId = backStack.arguments?.getString("taskId")?.toIntOrNull()
            if (taskId != null) {
                EditTaskScreen(
                    navController,
                    taskId,
                    appState = appState
                )
            }
        }
    }
}