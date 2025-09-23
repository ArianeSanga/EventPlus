package com.arianesanga.event.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EventoViewModel(private val repository: EventoRepository): ViewModel(){


    //listar eventos observavel pela UI
    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> get() = _eventos

    //funcao para carregar eventos no banco
    fun carregarEventos(){
        viewModelScope.launch {
            _eventos.value = repository.getAllEventos()
        }
    }

    fun adicionarEvento(evento: Evento){
        viewModelScope.launch {
            repository.insert(evento)
            carregarEventos()//atualiza a lista depois de adicionar
        }
    }
}
