package com.arianesanga.event.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val uid: String, // UID do Firebase
    val fullname: String,
    val username: String,
    val email: String,
    val phone: String,
    val photoUri: String? = null
)