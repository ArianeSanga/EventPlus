package com.arianesanga.event.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConvidadoViewModel(private val repository: ConvidadoRepository) : ViewModel() {

    private val _convidados = MutableStateFlow<List<Convidado>>(emptyList())
    val convidados: StateFlow<List<Convidado>> get() = _convidados

    private val db = FirebaseFirestore.getInstance()


    fun carregarConvidados(eventoId: Int) {
        viewModelScope.launch {
            repository.listarPorEvento(eventoId).collect { lista ->
                _convidados.value = lista
            }
        }
    }


    private fun adicionarConvidadoLocal(convidado: Convidado) {
        viewModelScope.launch {
            try {
                repository.inserir(convidado)
                carregarConvidados(convidado.eventoId)
                Log.d("ConvidadoViewModel", "Convidado salvo localmente: ${convidado.nome}")
            } catch (e: Exception) {
                Log.e("ConvidadoViewModel", "Erro ao salvar localmente: ${e.message}")
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

        db.collection("convidados")
            .add(convidadoMap)
            .addOnSuccessListener {
                Log.d("ConvidadoViewModel", "Convidado adicionado no Firestore: ${convidado.nome}")
            }
            .addOnFailureListener { e ->
                Log.e("ConvidadoViewModel", "Erro ao adicionar no Firestore: ${e.message}")
            }
    }


    fun criarContaEAdicionarConvidado(convidado: Convidado, senha: String) {
        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(convidado.email, senha)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                Log.d("ConvidadoViewModel", "Conta criada para ${convidado.email}, UID: $uid")

                // 1. Cria uma nova cópia do convidado com o UID do Firebase
                val convidadoComUid = convidado.copy(firebaseUid = uid)

                // 2. Salva localmente (agora sem o campo 'senha' no Model)
                adicionarConvidadoLocal(convidadoComUid)

                // 3. Salva no Firestore (melhor usar o UID como ID do documento para evitar duplicidade)
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
                    // Mapeia o documento do Firestore para o modelo local
                    val convidadoFirebase = Convidado(
                        // O ID local é 0, mas será ignorado ou corrigido pelo Repository/DAO
                        id = 0,
                        nome = doc.getString("nome") ?: "",
                        telefone = doc.getString("telefone") ?: "",
                        eventoId = (doc.getLong("eventoId")?.toInt() ?: eventoId),
                        email = doc.getString("email") ?: "",
                        firebaseUid = doc.getString("firebaseUid") // Recebe o UID
                    )

                    // **Ação Principal:** Chama a nova lógica de sincronização
                    viewModelScope.launch {
                        try {
                            repository.sincronizarConvidado(convidadoFirebase)
                            Log.d("ConvidadoViewModel", "Sincronizado via Firebase: ${convidadoFirebase.nome}")
                        } catch (e: Exception) {
                            Log.e("ConvidadoViewModel", "Erro ao sincronizar: ${e.message}")
                        }
                    }
                }
            }
    }
}
