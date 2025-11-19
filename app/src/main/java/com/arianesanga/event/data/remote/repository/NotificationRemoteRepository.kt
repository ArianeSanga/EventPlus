package com.arianesanga.event.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRemoteRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createNotification(userUid: String, data: Map<String, Any>) {
        db.collection("notifications")
            .document(userUid)
            .collection("items")
            .add(data)
            .await()
    }
}