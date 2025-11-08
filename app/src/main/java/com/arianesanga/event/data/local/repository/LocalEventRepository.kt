package com.arianesanga.event.data.local.repository

import com.arianesanga.event.data.local.dao.EventDao
import com.arianesanga.event.data.local.model.Event

class LocalEventRepository(private val eventDao: EventDao) {
    suspend fun insertOrUpdateEvent(event: Event) {
        val existing = eventDao.getEventById(event.id)
        if (existing == null) {
            eventDao.insert(event)
        } else {
            eventDao.update(event)
        }
    }

    suspend fun deleteEvent(event: Event) = eventDao.delete(event)
    suspend fun getEventsByUser(userUid: String) = eventDao.getEventsByUser(userUid)
    suspend fun getEventById(id: String) = eventDao.getEventById(id)
}