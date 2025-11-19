package com.arianesanga.event.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @ColumnInfo(name = "datetime")
    val dateTime: Long,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "budget")
    val budget: Double,

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,

    @ColumnInfo(name = "weather_temp")
    val weatherTemp: Double? = null,

    @ColumnInfo(name = "weather_desc")
    val weatherDesc: String? = null,

    @ColumnInfo(name = "weather_icon")
    val weatherIcon: String? = null,

    @ColumnInfo(name = "weather_feels_like")
    var weatherFeelsLike: Double? = null,

    @ColumnInfo(name = "weather_humidity")
    var weatherHumidity: Int? = null,

    @ColumnInfo(name = "weather_wind_speed")
    var weatherWindSpeed: Double? = null
)