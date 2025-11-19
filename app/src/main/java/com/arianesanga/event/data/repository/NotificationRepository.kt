package com.arianesanga.event.data.repository

import com.arianesanga.event.data.local.dao.NotificationDao
import com.arianesanga.event.data.local.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationDao) {

    fun notificationsFlow(): Flow<List<NotificationEntity>> = dao.getAllNotificationsFlow()

    fun unreadCountFlow(): Flow<Int> = dao.unreadCountFlow()

    suspend fun insert(notification: NotificationEntity): Long = dao.insert(notification)

    suspend fun markRead(id: Long) = dao.markRead(id)

    suspend fun markAllRead() = dao.markAllRead()
}