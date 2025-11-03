package com.arianesanga.event.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arianesanga.event.data.model.Convidado
import com.arianesanga.event.data.repository.ConvidadoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConvidadoViewModel(private val repository: ConvidadoRepository) : ViewModel() {

    private val _convidados = MutableStateFlow<List<Convidado>>(emptyList())
    val convidados: StateFlow<List<Convidado>> get() = _convidados

    private val _eventoId = MutableStateFlow(-1)

    val eventoIdAtual: Int?
        get() = _eventoId.value.takeIf { it > 0 }

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun setEventoId(id: Int) {
        _eventoId.value = id
    }

    private fun adicionarConvidadoLocal(convidado: Convidado) {
        viewModelScope.launch {
            try {
                repository.inserir(convidado)

                carregarConvidados()
                Log.d("ConvidadoViewModel", "Convidado salvo localmente: ${convidado.nome}")
            } catch (e: Exception) {
                Log.e("ConvidadoViewModel", "Erro ao salvar localmente: ${e.message}")
            }
        }
    }

    fun carregarConvidados() {
        viewModelScope.launch {
            val currentEventoId = _eventoId.value
            if (currentEventoId > 0) {
                repository.listarPorEvento(currentEventoId).collect { lista ->
                    _convidados.value = lista
                }
            } else {
                Log.e("ConvidadoViewModel", "EventoId inválido ao carregar convidados.")
                _convidados.value = emptyList()
            }
        }
    }

    private fun adicionarConvidadoFirebase(convidado: Convidado) {
        val convidadoMap = hashMapOf(
            "nome" to convidado.nome,
            "telefone" to convidado.telefone,
            "eventoId" to convidado.eventoId,
            "email" to convidado.email,
            "firebaseUid" to convidado.firebaseUid
        )


        val docId = convidado.firebaseUid
        if (docId == null) {
            Log.e("ConvidadoViewModel", "UID nulo, não foi possível salvar no Firestore.")
            return
        }

        db.collection("convidados")
            .document(docId) // Define o UID como o nome do documento
            .set(convidadoMap)
            .addOnSuccessListener {
                Log.d("ConvidadoViewModel", "Convidado adicionado no Firestore: ${convidado.nome}")
            }
            .addOnFailureListener { e ->
                Log.e("ConvidadoViewModel", "Erro ao adicionar no Firestore: ${e.message}")
            }
    }


    fun criarContaEAdicionarConvidado(convidado: Convidado, senha: String) {

        val currentEventoId = _eventoId.value

        if (currentEventoId <= 0) {
            Log.e("ConvidadoViewModel", "Falha: EventoId não definido ou inválido.")
            return
        }

        auth.createUserWithEmailAndPassword(convidado.email, senha)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                Log.d("ConvidadoViewModel", "Conta criada para ${convidado.email}, UID: $uid")

                val convidadoComUid = convidado.copy(
                    firebaseUid = uid,
                    eventoId = currentEventoId // Sobrescreve o ID do evento com o ID interno
                )


                adicionarConvidadoLocal(convidadoComUid)
                adicionarConvidadoFirebase(convidadoComUid)
            }
            .addOnFailureListener { e ->
                Log.e("ConvidadoViewModel", "Erro ao criar conta: ${e.message}")
            }
    }


    fun observarConvidadosFirebase(eventoId: Int) {
        db.collection("convidados")
            .whereEqualTo("eventoId", eventoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ConvidadoViewModel", "Erro ao observar convidados: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.documents?.forEach { doc ->
                    val convidadoFirebase = Convidado(
                        id = 0,
                        nome = doc.getString("nome") ?: "",
                        telefone = doc.getString("telefone") ?: "",
                        // Garante que o ID local do evento está correto
                        eventoId = (doc.getLong("eventoId")?.toInt() ?: eventoId),
                        email = doc.getString("email") ?: "",
                        firebaseUid = doc.getString("firebaseUid")
                    )

                    viewModelScope.launch {
                        try {
                            repository.sincronizarConvidado(convidadoFirebase)
                            Log.d("ConvidadoViewModel", "Sincronizado via Firebase: ${convidadoFirebase.nome}")

                            carregarConvidados()

                        } catch (e: Exception) {
                            Log.e("ConvidadoViewModel", "Erro ao sincronizar: ${e.message}")
                        }
                    }
                }
            }
    }
}