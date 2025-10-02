package com.arianesanga.event.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth

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

    fun deletarEvento(evento: Evento) {
        viewModelScope.launch {
            repository.delete(evento)
            carregarEventos()
        }
    }
    fun sincronizarEventosFirebase() {
        viewModelScope.launch {
            val eventosAntigos = repository.getAllEventos() // pega todos os eventos do SQLite
            val db = FirebaseFirestore.getInstance()

            eventosAntigos.forEach { evento ->
                val eventoMap = hashMapOf(
                    "id" to evento.id,
                    "nome" to evento.nome,
                    "descricao" to evento.descricao,
                    "data" to evento.data,
                    "local" to evento.local,
                    "orcamento" to evento.orcamento
                )

                db.collection("eventos")
                    .document(evento.id.toString()) // garante que não vai duplicar
                    .set(eventoMap)
                    .addOnSuccessListener {
                        Log.d("EventoViewModel", "Evento sincronizado: ${evento.nome}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("EventoViewModel", "Erro ao sincronizar evento: ${e.message}")
                    }
            }
        }
    }


}
