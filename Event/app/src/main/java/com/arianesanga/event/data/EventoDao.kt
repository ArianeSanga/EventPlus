package com.arianesanga.event.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EventoDao{
    @Insert//insere um evento
    suspend fun inserir(evento: Evento)

    @Query("SELECT * FROM eventos")//busca todos os eventos no banco
    suspend fun listarTodos(): List<Evento>
}