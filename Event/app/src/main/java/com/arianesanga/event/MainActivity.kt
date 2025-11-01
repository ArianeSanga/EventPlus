package com.arianesanga.event

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue // NOVO IMPORT
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue // NOVO IMPORT
import com.arianesanga.event.ui.theme.EventTheme
import com.arianesanga.event.views.CadastroActivity
import com.arianesanga.event.views.ConvidadoLoginActivity
import com.arianesanga.event.views.Login
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // --- NOVO ESTADO (Nível da Classe) ---
    // Precisamos que o Composable (Login) e os callbacks (launcher)
    // acessem as mesmas variáveis.
    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)
    // --- FIM NOVO ESTADO ---

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
                isLoading = false // Resetar
            }
        } else {
            // Usuário cancelou ou houve outro erro
            isLoading = false // Resetar
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
                    // Passa o estado atual para o Composable
                    isLoading = isLoading,
                    errorMessage = errorMessage,

                    onLoginWithGoogle = {
                        isLoading = true // Define loading
                        errorMessage = null // Limpa erro
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    onLoginConvidado = {
                        val intent = Intent(this, ConvidadoLoginActivity::class.java)
                        intent.putExtra("eventoId", 1)
                        startActivity(intent)
                    },
                    onLoginWithEmailPassword = ::firebaseAuthWithEmailPassword, // Passa a função direto
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
                isLoading = false // Reseta em sucesso ou falha
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Tenta logar o usuário com email e senha no Firebase.
     * AGORA ATUALIZA O ESTADO 'isLoading' E 'errorMessage'.
     */
    private fun firebaseAuthWithEmailPassword(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Preencha o Email e a Senha para logar."
            return // Não seta loading
        }

        isLoading = true // Começa o loading
        errorMessage = null // Limpa erro

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    val error = task.exception?.message ?: "Erro desconhecido ao logar."
                    errorMessage = "Falha no Login: $error"
                }
                isLoading = false // Reseta em sucesso ou falha
            }
    }

    private fun navigateToCadastroScreen() {
        val intent = Intent(this, CadastroActivity::class.java)
        startActivity(intent)
    }
}