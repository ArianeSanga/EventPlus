package com.arianesanga.event.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue // NOVO IMPORT
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue // NOVO IMPORT
import com.arianesanga.event.R
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.ui.screens.Login
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Estado para controle de loading e erros (usado no Google Sign-In)
    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                errorMessage = "Falha no login com Google: ${e.message}"
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (auth.currentUser != null) {
            navigateToHome()
            return
        }

        setContent {
            EventTheme {
                Login(
                    isLoading = isLoading,
                    errorMessage = errorMessage,

                    onLoginWithGoogle = {
                        isLoading = true
                        errorMessage = null
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    onLoginConvidado = {
                        val intent = Intent(this, ConvidadoLoginActivity::class.java)
                        intent.putExtra("eventoId", 1)
                        startActivity(intent)
                    },
                    onNavigateToEmailLogin = ::navigateToEmailLoginScreen, // <<< NOVA CHAMADA
                    onNavigateToCadastro = ::navigateToCadastroScreen
                )
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: com.google.android.gms.tasks.Task<AuthResult> ->
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    errorMessage = "Falha na Autenticação com Google."
                }
                isLoading = false
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Função de login com email/senha REMOVIDA daqui

    private fun navigateToCadastroScreen() {
        val intent = Intent(this, CadastroActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToEmailLoginScreen() {
        val intent = Intent(this, EmailLoginActivity::class.java)
        startActivity(intent)
    }
}