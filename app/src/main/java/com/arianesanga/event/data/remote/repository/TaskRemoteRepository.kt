package com.arianesanga.event.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TaskRemoteRepository {
    private val db = FirebaseFirestore.getInstance()

    fun createOrUpdateTask(taskId: String, data: Map<String, Any>, onComplete: (Boolean, String?) -> Unit) {
        db.collection("task").document(taskId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun deleteTask(taskId: String, onComplete: (Boolean) -> Unit) {
        db.collection("task").document(taskId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getTasksByEvent(eventId: Int, onComplete: (List<Map<String, Any>>?) -> Unit) {
        db.collection("task")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull { it.data })
            }
            .addOnFailureListener { onComplete(null) }
    }
}