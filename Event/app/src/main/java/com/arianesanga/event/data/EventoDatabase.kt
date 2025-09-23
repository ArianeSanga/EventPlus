package com.arianesanga.event.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

//declarando todas as entidades do banco e a versao
@Database(entities = [Evento::class], version = 1, exportSchema = false)
abstract class EventoDatabase : RoomDatabase(){


    abstract fun eventoDao(): EventoDao

    companion object{
        @Volatile
        private var INSTANCE: EventoDatabase? = null

        //função que pega a instancia do banco de dados
        fun getDatabase(context: Context): EventoDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventoDatabase::class.java,
                    "EventoDatabase"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}