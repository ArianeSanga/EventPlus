package com.arianesanga.event.data.local.model

import androidx.room.*

@Entity(tableName = "guest")
data class Guest(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "event_id")
    val eventId: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "phone")
    val phone: String? = null,

    @ColumnInfo(name = "status")
    val status: String = "pending",

    @ColumnInfo(name = "firebase_uid")
    val firebaseUid: String? = null
)