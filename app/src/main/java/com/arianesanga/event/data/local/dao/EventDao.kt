package com.arianesanga.event.data.local.dao

import androidx.room.*
import com.arianesanga.event.data.local.model.Event

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("SELECT * FROM event WHERE userUid = :userUid")
    suspend fun getEventsByUser(userUid: String): List<Event>

    @Query("SELECT * FROM event WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): Event?
}