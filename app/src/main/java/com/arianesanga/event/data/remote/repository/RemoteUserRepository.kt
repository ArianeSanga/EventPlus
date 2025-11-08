package com.arianesanga.event.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RemoteUserRepository {
    private val db = FirebaseFirestore.getInstance()

    fun createUser(userUid: String, data: Map<String, Any>, onComplete: (Boolean, String?) -> Unit) {
        db.collection("user").document(userUid)
            .set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun updateUser(userUid: String, data: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        db.collection("user").document(userUid)
            .set(data, SetOptions.merge()) // MERGE → atualiza sem sobrescrever tudo
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getUser(userUid: String, onComplete: (Map<String, Any>?) -> Unit) {
        db.collection("user").document(userUid)
            .get()
            .addOnSuccessListener { doc -> onComplete(doc.data) }
            .addOnFailureListener { onComplete(null) }
    }

    fun deleteUser(userUid: String, onComplete: (Boolean) -> Unit) {
        db.collection("user").document(userUid)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}