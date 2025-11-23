package com.arianesanga.event.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arianesanga.event.ui.components.rememberAppState
import com.arianesanga.event.ui.screens.*

@Composable
fun AppNavHost(navController: NavHostController, initialInviteEventId: String? = null) {

    val appState = rememberAppState(navController)

    NavHost(navController = navController, startDestination = "main") {

        composable("main") { MainScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }

        composable("home") {
            HomeScreen(navController, appState = appState)
        }

        composable("create_event") {
            CreateEventScreen(navController, appState = appState)
        }

        composable("event_list") {
            EventListScreen(navController, appState = appState)
        }

        composable("profile") {
            ProfileScreen(navController, appState = appState)
        }

        composable("edit_profile") {
            EditProfileScreen(navController, appState = appState)
        }

        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }

        composable("edit_event/{id}") { backStack ->
            backStack.arguments?.getString("id")?.toIntOrNull()?.let { id ->
                EditEventScreen(navController, id, appState = appState)
            }
        }

        composable("event_details/{id}") { backStack ->
            backStack.arguments?.getString("id")?.toIntOrNull()?.let { id ->
                EventDetailsScreen(navController, id, appState = appState)
            }
        }

        composable("tasks/{eventId}") { backStack ->
            backStack.arguments?.getString("eventId")?.toIntOrNull()?.let { eventId ->
                TaskListScreen(navController, eventId, appState = appState)
            }
        }

        composable("create_task/{eventId}") { backStack ->
            backStack.arguments?.getString("eventId")?.toIntOrNull()?.let { eventId ->
                CreateTaskScreen(navController, eventId, appState = appState)
            }
        }

        composable("edit_task/{taskId}") { backStack ->
            backStack.arguments?.getString("taskId")?.toIntOrNull()?.let { taskId ->
                EditTaskScreen(navController, taskId, appState = appState)
            }
        }

        /* 💎 TELA DE CONVIDADOS */
        composable("event_guests/{eventId}") { backStack ->
            backStack.arguments?.getString("eventId")?.toIntOrNull()?.let { eventId ->
                EventGuestsScreen(eventId = eventId, navController = navController)
            }
        }

        /* ⭐ NOVA TELA PARA DEEP LINK DE CONVITE */
        composable("invite_screen/{eventId}") { backStack ->
            backStack.arguments?.getString("eventId")?.toIntOrNull()?.let { eventId ->
                InviteScreen(eventId = eventId, navController = navController)
            }
        }
    }

    /* ⭐ Deep link abre a tela de convite */
    LaunchedEffect(initialInviteEventId) {
        initialInviteEventId?.let { id ->
            navController.navigate("invite_screen/$id") {
                launchSingleTop = true
            }
        }
    }
}
