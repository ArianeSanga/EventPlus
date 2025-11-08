package com.arianesanga.event.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "event")
data class Event(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userUid: String,
    val name: String,
    val description: String,
    val date: String,
    val location: String,
    val budget: Double,
    val imageUri: String? = null
)