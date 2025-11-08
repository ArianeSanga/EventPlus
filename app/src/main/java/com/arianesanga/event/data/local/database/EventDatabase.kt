package com.arianesanga.event.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arianesanga.event.data.local.dao.UserDao
import com.arianesanga.event.data.local.dao.EventDao
import com.arianesanga.event.data.local.model.User
import com.arianesanga.event.data.local.model.Event

@Database(
    entities = [User::class, Event::class],
    version = 2,
    exportSchema = false
)
abstract class EventDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: EventDatabase? = null

        fun getDatabase(context: Context): EventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "EventDatabase"
                )
                    .fallbackToDestructiveMigration()
                    .setJournalMode(JournalMode.TRUNCATE)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}