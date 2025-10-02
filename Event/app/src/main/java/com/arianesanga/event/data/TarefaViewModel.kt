package com.arianesanga.event.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// O ViewModel só aceita o Repository
class TarefaViewModel(private val repository: TarefaRepository) : ViewModel() {

    private val _tarefas = MutableStateFlow<List<Tarefa>>(emptyList())
    val tarefas: StateFlow<List<Tarefa>> get() = _tarefas

    private var eventoAtualId: Int = -1

    fun carregarTarefas(eventoId: Int) {
        if (eventoId != -1 && eventoId != eventoAtualId) {
            eventoAtualId = eventoId
            viewModelScope.launch {
                repository.listarPorEvento(eventoId).collect { lista ->
                    _tarefas.value = lista
                }
            }
        }
    }

    fun adicionarTarefa(descricao: String, dataLimite: String? = null) {
        if (eventoAtualId != -1 && descricao.isNotBlank()) {
            val novaTarefa = Tarefa(
                descricao = descricao,
                eventoId = eventoAtualId,
                dataLimite = dataLimite
            )
            viewModelScope.launch {
                repository.inserirOuAtualizar(novaTarefa)
            }
        }
    }

    fun alternarConclusao(tarefa: Tarefa) {
        val tarefaAtualizada = tarefa.copy(concluida = !tarefa.concluida)
        viewModelScope.launch {
            repository.atualizar(tarefaAtualizada)
        }
    }

    fun deletarTarefa(tarefa: Tarefa) {
        viewModelScope.launch {
            repository.deletar(tarefa)
        }
    }
}