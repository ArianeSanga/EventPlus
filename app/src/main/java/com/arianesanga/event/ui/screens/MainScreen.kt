package com.arianesanga.event.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arianesanga.event.R
import com.arianesanga.event.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun MainScreen(navController: NavController) {

    val context = LocalContext.current
    val activity = context as Activity
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    FirebaseApp.initializeApp(context)
    val auth = remember { FirebaseAuth.getInstance() }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                isLoading = true
                auth.signInWithCredential(credential)
                    .addOnCompleteListener {
                        isLoading = false
                        if (it.isSuccessful) {
                            navController.navigate("home") {
                                popUpTo("main") { inclusive = true }
                            }
                        } else {
                            errorMessage = "Falha na autenticação com Google."
                        }
                    }
            }
            catch (e: Exception) {
                isLoading = false
                errorMessage = "Erro no login Google: ${e.message}"
            }
        }
    }

    if (auth.currentUser != null) {
        LaunchedEffect(Unit) {
            navController.navigate("home") {
                popUpTo("main") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(DARKBLUE, MEDIUMBLUE, DARKBLUE))
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = "EventPlus",
            color = WHITE,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
            style = LocalTextStyle.current.copy(
                shadow = Shadow(
                    color = LIGHTBLUE,
                    offset = Offset(0f, 0f),
                    blurRadius = 25f
                )
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(300.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("login") },
            colors = ButtonDefaults.buttonColors(containerColor = PINK),
            modifier = Modifier.fillMaxWidth().height(55.dp)
        ) {
            Icon(Icons.Filled.Email, contentDescription = null, tint = WHITE)
            Spacer(Modifier.width(8.dp))
            Text("ENTRAR COM EMAIL", color = WHITE)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
            modifier = Modifier.fillMaxWidth().height(55.dp)
        ) {
            Text("ENTRAR COM GOOGLE", color = WHITE)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Não tem uma conta? CADASTRE-SE",
            color = PINK,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                navController.navigate("register")
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        TermsAndPrivacyDialog()
    }
}

@Composable
fun TermsAndPrivacyDialog() {
    var showDialog by remember { mutableStateOf(false) }
    var isPrivacy by remember { mutableStateOf(false) }

    val annotatedText = buildAnnotatedString {
        pushStringAnnotation(tag = "TERMS", annotation = "TERMS")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = PINK)) {
            append("Termos de Uso")
        }
        pop()
        append(" e ")
        pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = PINK)) {
            append("Política de Privacidade")
        }
        pop()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 15.dp)) {
        Text(
            text = "Ao continuar, você concorda com nossos",
            color = WHITE.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        ClickableText(
            text = annotatedText,
            style = LocalTextStyle.current.copy(
                color = WHITE.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            ),
            onClick = { offset ->
                annotatedText.getStringAnnotations("TERMS", offset, offset)
                    .firstOrNull()?.let { isPrivacy = false; showDialog = true }
                annotatedText.getStringAnnotations("PRIVACY", offset, offset)
                    .firstOrNull()?.let { isPrivacy = true; showDialog = true }
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (isPrivacy) "Política de Privacidade" else "Termos de Uso") },
            text = {
                Text(
                    if (isPrivacy) "Aqui entra a Política de Privacidade resumida..." else "Aqui entram os Termos de Uso resumidos..."
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("FECHAR", color = PINK)
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}