package com.arianesanga.event.data.repository

import com.arianesanga.event.data.dao.TarefaDao
import com.arianesanga.event.data.model.Tarefa
import kotlinx.coroutines.flow.Flow

class TarefaRepository(private val tarefaDao: TarefaDao) {

    suspend fun inserirOuAtualizar(tarefa: Tarefa): Long {
        return tarefaDao.inserir(tarefa)
    }

    suspend fun atualizar(tarefa: Tarefa) {
        tarefaDao.atualizar(tarefa)
    }

    suspend fun deletar(tarefa: Tarefa) {
        tarefaDao.deletar(tarefa)
    }

    fun listarPorEvento(eventoId: Int): Flow<List<Tarefa>> = tarefaDao.listarPorEvento(eventoId)
}
