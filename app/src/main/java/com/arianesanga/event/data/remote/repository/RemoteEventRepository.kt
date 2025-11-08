package com.arianesanga.event.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RemoteEventRepository {
    private val db = FirebaseFirestore.getInstance()

    fun createOrUpdateEvent(
        userUid: String,
        id: String,
        data: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.collection("user").document(userUid)
            .collection("event").document(id)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun deleteEvent(userUid: String, id: String, onComplete: (Boolean) -> Unit) {
        db.collection("user").document(userUid)
            .collection("event").document(id)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getEventsByUser(userUid: String, onComplete: (List<Map<String, Any>>?) -> Unit) {
        db.collection("user").document(userUid)
            .collection("event")
            .get()
            .addOnSuccessListener { snapshot ->
                val events = snapshot.documents.mapNotNull { it.data }
                onComplete(events)
            }
            .addOnFailureListener { onComplete(null) }
    }
}