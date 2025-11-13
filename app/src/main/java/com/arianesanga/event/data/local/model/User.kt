package com.arianesanga.event.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "fullname")
    val fullname: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "photoUri")
    val photoUri: String? = null
)