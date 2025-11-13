package com.arianesanga.event.data.local.model

import androidx.room.*

@Entity(tableName = "event")
data class Event(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "user_uid")
    val userUid: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "budget")
    val budget: Double,

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null
)