package com.arianesanga.event.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth

class EventoViewModel(private val repository: EventoRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> get() = _eventos

    // Carrega eventos do Room
    fun carregarEventos() {
        viewModelScope.launch {
            val ownerUid = auth.currentUser?.uid
            if (ownerUid != null) {
                val lista = repository.getEventosByOwner(ownerUid)
                _eventos.value = lista // substitui toda a lista, nunca somar
            } else {
                _eventos.value = emptyList()
            }
        }
    }

    // Adiciona um evento novo
    fun adicionarEvento(evento: Evento) {
        viewModelScope.launch {
            val ownerUid = auth.currentUser?.uid ?: return@launch
            val eventoComOwner = evento.copy(ownerUid = ownerUid)
            repository.insert(eventoComOwner)
            carregarEventos() // só precisa atualizar a lista uma vez
            // sincronizarEventosFirebase() pode ser chamado separadamente se quiser
        }
    }

    // Deleta um evento
    fun deletarEvento(evento: Evento) {
        viewModelScope.launch {
            repository.delete(evento)
            carregarEventos() // atualiza a lista
        }
    }

    // Sincronização com Firebase (não atualiza o Flow)
    fun sincronizarEventosFirebase() {
        viewModelScope.launch {
            val ownerUid = auth.currentUser?.uid ?: return@launch
            val eventosLocais = repository.getEventosByOwner(ownerUid)
            val db = FirebaseFirestore.getInstance()

            eventosLocais.forEach { evento ->
                val eventoMap = hashMapOf(
                    "ownerUid" to ownerUid,
                    "nome" to evento.nome,
                    "descricao" to evento.descricao,
                    "data" to evento.data,
                    "local" to evento.local,
                    "orcamento" to evento.orcamento
                )

                db.collection("eventos")
                    .document(evento.id.toString())
                    .set(eventoMap)
            }
        }
    }

    // Baixa eventos do Firebase para o Room
    fun baixarEventosFirebase() {
        val ownerUid = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("eventos")
            .whereEqualTo("ownerUid", ownerUid)
            .get()
            .addOnSuccessListener { documents ->
                viewModelScope.launch {
                    // Criamos uma lista temporária de eventos
                    val eventosBaixados = documents.mapNotNull { doc ->
                        val id = doc.id.toIntOrNull() ?: 0
                        Evento(
                            id = id,
                            nome = doc.getString("nome") ?: "",
                            descricao = doc.getString("descricao") ?: "",
                            data = doc.getString("data") ?: "",
                            local = doc.getString("local") ?: "",
                            orcamento = doc.getDouble("orcamento") ?: 0.0,
                            ownerUid = doc.getString("ownerUid")
                        )
                    }

                    // Inserimos TODOS de uma vez, evitando múltiplas atualizações do Flow
                    eventosBaixados.forEach { evento ->
                        repository.insert(evento) // REPLACE já evita duplicação no Room
                    }

                    // Atualiza a UI apenas uma vez depois de inserir tudo
                    carregarEventos()
                }
            }
    }
}
