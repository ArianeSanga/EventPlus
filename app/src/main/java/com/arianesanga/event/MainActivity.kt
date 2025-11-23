package com.arianesanga.event

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.arianesanga.event.notifications.NotificationHelper
import com.arianesanga.event.ui.navigation.AppNavHost
import com.arianesanga.event.ui.theme.EventTheme

class MainActivity : ComponentActivity() {

    private var initialInviteEventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apenas extrai o eventId — SEM mexer mais nada
        initialInviteEventId = getEventIdFromIntent(intent)

        NotificationHelper.createChannel(this)

        setContent {
            EventTheme(darkTheme = true, dynamicColor = false)  {
                RequestNotificationPermissionIfNeeded()
                val navController = rememberNavController()

                // Apenas passa o eventId se existir
                AppNavHost(
                    navController = navController,
                    initialInviteEventId = initialInviteEventId
                )
            }
        }
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)

        val newId = getEventIdFromIntent(newIntent)
        if (!newId.isNullOrBlank()) {
            initialInviteEventId = newId
            setIntent(newIntent)
        }
    }


    private fun getEventIdFromIntent(intent: Intent?): String? {
        if (intent == null) return null

        val data: Uri? = intent.data
        val queryId = data?.getQueryParameter("eventId")

        if (!queryId.isNullOrBlank()) return queryId

        val extraId = intent.getStringExtra("eventId")
        if (!extraId.isNullOrBlank()) return extraId

        return null
    }
}

@Composable
fun RequestNotificationPermissionIfNeeded() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(context, "Permissão para notificações negada.", Toast.LENGTH_SHORT).show()
            }
        }
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
