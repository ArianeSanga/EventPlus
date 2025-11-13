package com.arianesanga.event.data.local.model

import androidx.room.*

@Entity(tableName = "task")
data class Task(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "event_id") val eventId: Int,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "deadline")
    val deadline: String? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
)