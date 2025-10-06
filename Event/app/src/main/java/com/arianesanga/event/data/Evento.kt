package com.arianesanga.event.data

import androidx.room.Entity;//importa biblioteca romm que usaremos para faciliar no banco de dados
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class Evento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val descricao : String,
    val data: String,
    val local: String,
    val orcamento: Double,
    val ownerUid: String? = null,
    val fotoUrl: String = ""
)
