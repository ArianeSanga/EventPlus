package com.arianesanga.event.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Declarando todas as entidades do banco e a versão
@Database(entities = [Evento::class, Convidado::class,Tarefa::class], version = 2, exportSchema = false)
abstract class EventoDatabase : RoomDatabase() {
    abstract fun eventoDao(): EventoDao
    abstract fun convidadoDao(): ConvidadoDao
    abstract fun tarefaDao(): TarefaDao

    companion object {
        @Volatile
        private var INSTANCE: EventoDatabase? = null

        fun getDatabase(context: Context): EventoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventoDatabase::class.java,
                    "EventoDatabase"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = instance
                    instance
            }
        }
    }
}
