package com.arianesanga.event.data.repository

import android.net.Uri
import com.arianesanga.event.data.local.dao.EventDao
import com.arianesanga.event.data.local.model.Event
import com.arianesanga.event.data.remote.repository.EventRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository(
    private val local: EventDao,
    private val remote: EventRemoteRepository
) {

    suspend fun getEventsByUser(userUid: String): List<Event> = withContext(Dispatchers.IO) {
        local.getEventsByUser(userUid)
    }

    suspend fun insertEvent(event: Event, imagePath: String?): Long = withContext(Dispatchers.IO) {

        val generatedId = local.insertEvent(event)

        val finalEvent = event.copy(
            id = generatedId.toInt(),
            imageUri = imagePath
        )

        local.updateEvent(finalEvent)

        val dataFirestore = mutableMapOf<String, Any>(
            "id" to finalEvent.id,
            "userUid" to finalEvent.userUid,
            "name" to finalEvent.name,
            "description" to finalEvent.description,
            "datetime" to finalEvent.dateTime,
            "location" to finalEvent.location,
            "budget" to finalEvent.budget
        )

        imagePath?.let { dataFirestore["imageUri"] = it }

        remote.createOrUpdateEvent(
            finalEvent.id.toString(),
            dataFirestore
        ) { _, _ -> }

        return@withContext generatedId
    }

    suspend fun updateEvent(event: Event, newImageUri: Uri?) =
        withContext(Dispatchers.IO) {

            var newImageUrl: String? = event.imageUri

            if (newImageUri != null) {
                newImageUrl = newImageUri.toString()
            }

            val updatedEvent = event.copy(imageUri = newImageUrl)

            local.updateEvent(updatedEvent)

            val data = mutableMapOf<String, Any>(
                "id" to updatedEvent.id,
                "userUid" to updatedEvent.userUid,
                "name" to updatedEvent.name,
                "description" to updatedEvent.description,
                "datetime" to updatedEvent.dateTime,
                "location" to updatedEvent.location,
                "budget" to updatedEvent.budget
            )

            newImageUrl?.let { data["imageUri"] = it }

            remote.createOrUpdateEvent(
                updatedEvent.id.toString(),
                data
            ) { _, _ -> }
        }

    suspend fun deleteEvent(event: Event) = withContext(Dispatchers.IO) {
        local.deleteEvent(event)
        remote.deleteEvent(event.id.toString()) { _ -> }
    }
}