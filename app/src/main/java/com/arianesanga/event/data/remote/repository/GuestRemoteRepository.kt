package com.arianesanga.event.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GuestRemoteRepository {

    private val db = FirebaseFirestore.getInstance()

    // ---------------------------------------------------------
    //  ➤ CRIAR / ATUALIZAR
    // ---------------------------------------------------------
    fun createOrUpdateGuest(
        guestId: String,
        data: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.collection("guest").document(guestId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    // ---------------------------------------------------------
    //  ➤ DELETAR
    // ---------------------------------------------------------
    fun deleteGuest(guestId: String, onComplete: (Boolean) -> Unit) {
        db.collection("guest").document(guestId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ---------------------------------------------------------
    //  ➤ BUSCAR (ASSÍNCRONO NORMAL – CALLBACK)
    // ---------------------------------------------------------
    fun getGuestsByEvent(eventId: Int, onComplete: (List<Map<String, Any>>?) -> Unit) {
        db.collection("guest")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.data }
                onComplete(list)
            }
            .addOnFailureListener { onComplete(null) }
    }

    // ---------------------------------------------------------
    //  ➤ BUSCAR (SÍNCRONO PARA COROUTINES – SEM await())
    // ---------------------------------------------------------
    suspend fun getGuestsByEventSync(eventId: Int): List<Map<String, Any>>? =
        suspendCancellableCoroutine { cont ->
            db.collection("guest")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener { snap ->
                    cont.resume(snap.documents.mapNotNull { it.data })
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
}
