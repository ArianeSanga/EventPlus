package com.arianesanga.event.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.arianesanga.event.data.model.Convidado
import kotlinx.coroutines.flow.Flow

@Dao
interface ConvidadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(convidado: Convidado)

    @Update
    suspend fun atualizar(convidado: Convidado)

    @Query("SELECT * FROM convidados WHERE eventoId = :eventoId")
    fun listarPorEvento(eventoId: Int): Flow<List<Convidado>>

    @Query("SELECT * FROM convidados WHERE firebaseUid = :firebaseUid LIMIT 1")
    suspend fun buscarPorFirebaseUid(firebaseUid: String): Convidado?
}