package com.arianesanga.event.data.dao

import androidx.room.*
import com.arianesanga.event.data.model.Tarefa
import kotlinx.coroutines.flow.Flow

@Dao
interface TarefaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(tarefa: Tarefa): Long // RETORNA O ID NOVO

    @Update
    suspend fun atualizar(tarefa: Tarefa)

    @Delete
    suspend fun deletar(tarefa: Tarefa)

    @Query("SELECT * FROM tarefas WHERE eventoId = :eventoId ORDER BY concluida ASC, id DESC")
    fun listarPorEvento(eventoId: Int): Flow<List<Tarefa>>
}