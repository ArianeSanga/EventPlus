package com.arianesanga.event.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventoDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(evento: Evento)

    @Query("SELECT * FROM eventos WHERE ownerUid = :ownerUid")
    suspend fun listarPorOwner(ownerUid: String): List<Evento>

    @Delete
    suspend fun deletar(evento: Evento)
}
