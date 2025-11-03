package com.arianesanga.event.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tarefas")
data class Tarefa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descricao: String,
    val eventoId: Int, // Chave estrangeira para vincular ao evento
    val concluida: Boolean = false,
    val dataLimite: String? = null
)
