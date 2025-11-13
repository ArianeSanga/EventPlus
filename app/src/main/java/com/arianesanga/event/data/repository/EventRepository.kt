package com.arianesanga.event.data.repository

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

    suspend fun insertEvent(event: Event) = withContext(Dispatchers.IO) {
        // insere local e obtém rowId (Long)
        val rowId = local.insertEvent(event)
        val generatedId = rowId.toInt() // converte para Int se precisar
        val remoteId = generatedId.toString()

        // monta Map<String, Any> apenas com valores não-nulos
        val data = mutableMapOf<String, Any>()
        data["id"] = generatedId
        data["userUid"] = event.userUid
        data["name"] = event.name
        data["description"] = event.description
        data["date"] = event.date
        data["location"] = event.location
        data["budget"] = event.budget
        event.imageUri?.let { data["imageUri"] = it }

        remote.createOrUpdateEvent(remoteId, data) { success, err ->
            // opcional: log / tratamento de falha
        }
    }

    suspend fun updateEvent(event: Event) = withContext(Dispatchers.IO) {
        local.updateEvent(event)
        val remoteId = event.id.toString()

        val data = mutableMapOf<String, Any>()
        data["name"] = event.name
        data["description"] = event.description
        data["date"] = event.date
        data["location"] = event.location
        data["budget"] = event.budget
        event.imageUri?.let { data["imageUri"] = it }

        remote.createOrUpdateEvent(remoteId, data) { _, _ -> }
    }

    suspend fun deleteEvent(event: Event) = withContext(Dispatchers.IO) {
        local.deleteEvent(event)
        remote.deleteEvent(event.id.toString()) { _ -> }
    }
}