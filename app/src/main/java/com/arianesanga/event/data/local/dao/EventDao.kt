package com.arianesanga.event.data.local.dao

import androidx.room.*
import com.arianesanga.event.data.local.model.Event

@Dao
interface EventDao {

    @Query("SELECT * FROM event ORDER BY datetime ASC")
    suspend fun getAllEvents(): List<Event>

    @Query("SELECT * FROM event WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Int): Event?

    @Query("SELECT * FROM event WHERE user_uid = :userUid ORDER BY datetime ASC")
    suspend fun getEventsByUser(userUid: String): List<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM event WHERE id = :id")
    suspend fun deleteById(id: Int)
}