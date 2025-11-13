package com.arianesanga.event.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser

class AuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // ✅ Registro (apenas autenticação)
    fun registerUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) onSuccess(userId)
                    else onError("Erro ao obter UID do usuário.")
                } else {
                    val error = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "Email já cadastrado."
                        else -> task.exception?.message ?: "Erro desconhecido."
                    }
                    onError(error)
                }
            }
    }

    // ✅ Login
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Falha ao fazer login.")
                }
            }
    }

    // ✅ Obter usuário atual (Firebase)
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // ✅ Logout
    fun logout() {
        auth.signOut()
    }
}