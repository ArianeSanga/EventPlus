package com.arianesanga.event.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class GuestRemoteRepository {
    private val db = FirebaseFirestore.getInstance()

    fun createOrUpdateGuest(guestId: String, data: Map<String, Any>, onComplete: (Boolean, String?) -> Unit) {
        db.collection("guest").document(guestId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun deleteGuest(guestId: String, onComplete: (Boolean) -> Unit) {
        db.collection("guest").document(guestId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getGuestsByEvent(eventId: Int, onComplete: (List<Map<String, Any>>?) -> Unit) {
        db.collection("guest")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull { it.data })
            }
            .addOnFailureListener { onComplete(null) }
    }
}