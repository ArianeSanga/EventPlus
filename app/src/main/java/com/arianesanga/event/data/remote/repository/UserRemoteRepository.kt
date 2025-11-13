package com.arianesanga.event.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRemoteRepository {

    private val db = FirebaseFirestore.getInstance()

    // ✅ Criação do documento do usuário no Firestore
    suspend fun createUser(userUid: String, data: Map<String, Any>) {
        db.collection("users")
            .document(userUid)
            .set(data)
            .await()
    }

    // ✅ Atualização
    suspend fun updateUser(userUid: String, data: Map<String, Any>) {
        db.collection("users")
            .document(userUid)
            .set(data, SetOptions.merge())
            .await()
    }

    // ✅ Leitura
    suspend fun getUser(userUid: String): Map<String, Any>? {
        val snapshot = db.collection("users")
            .document(userUid)
            .get()
            .await()
        return snapshot.data
    }

    // ✅ Exclusão
    suspend fun deleteUser(userUid: String) {
        db.collection("users")
            .document(userUid)
            .delete()
            .await()
    }
}