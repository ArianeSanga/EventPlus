package com.arianesanga.event.data.local.dao

import androidx.room.*
import com.arianesanga.event.data.local.model.Guest

@Dao
interface GuestDao {

    @Query("SELECT * FROM guest")
    suspend fun getAllGuests(): List<Guest>

    @Query("SELECT * FROM guest WHERE event_id = :eventId")
    suspend fun getGuestsByEvent(eventId: Int): List<Guest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuest(guest: Guest): Long

    @Update
    suspend fun updateGuest(guest: Guest)

    @Delete
    suspend fun deleteGuest(guest: Guest)
}