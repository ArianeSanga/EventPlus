package com.arianesanga.event.data.repository

import com.arianesanga.event.data.local.dao.GuestDao
import com.arianesanga.event.data.local.model.Guest
import com.arianesanga.event.data.remote.repository.GuestRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GuestRepository(
    private val local: GuestDao,
    private val remote: GuestRemoteRepository
) {

    suspend fun getGuestsByEvent(eventId: Int): List<Guest> = withContext(Dispatchers.IO) {
        local.getGuestsByEvent(eventId)
    }

    suspend fun insertGuest(guest: Guest) = withContext(Dispatchers.IO) {
        val rowId = local.insertGuest(guest)
        val generatedId = rowId.toInt()
        val remoteId = generatedId.toString()

        val data = mutableMapOf<String, Any>()
        data["id"] = generatedId
        data["eventId"] = guest.eventId
        data["name"] = guest.name
        guest.email?.let { data["email"] = it }
        guest.phone?.let { data["phone"] = it }
        data["status"] = guest.status
        guest.firebaseUid?.let { data["firebaseUid"] = it }

        remote.createOrUpdateGuest(remoteId, data) { _, _ -> }
    }

    suspend fun updateGuest(guest: Guest) = withContext(Dispatchers.IO) {
        local.updateGuest(guest)
        val remoteId = guest.id.toString()

        val data = mutableMapOf<String, Any>()
        data["name"] = guest.name
        guest.email?.let { data["email"] = it }
        guest.phone?.let { data["phone"] = it }
        data["status"] = guest.status
        guest.firebaseUid?.let { data["firebaseUid"] = it }

        remote.createOrUpdateGuest(remoteId, data) { _, _ -> }
    }

    suspend fun deleteGuest(guest: Guest) = withContext(Dispatchers.IO) {
        local.deleteGuest(guest)
        remote.deleteGuest(guest.id.toString()) { _ -> }
    }
}