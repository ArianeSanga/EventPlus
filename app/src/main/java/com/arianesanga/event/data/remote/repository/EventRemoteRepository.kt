package com.arianesanga.event.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EventRemoteRepository {
    private val db = FirebaseFirestore.getInstance()

    fun createOrUpdateEvent(
        eventId: String,
        data: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.collection("event").document(eventId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun deleteEvent(eventId: String, onComplete: (Boolean) -> Unit) {
        db.collection("event").document(eventId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getEventsByUser(userUid: String, onComplete: (List<Map<String, Any>>?) -> Unit) {
        db.collection("event")
            .whereEqualTo("userUid", userUid)
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull { it.data })
            }
            .addOnFailureListener { onComplete(null) }
    }
}