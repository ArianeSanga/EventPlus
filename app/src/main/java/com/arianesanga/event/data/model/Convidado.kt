package com.arianesanga.event.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "convidados")
data class Convidado(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val telefone: String,
    val eventoId: Int,
    val email: String = "",
    val firebaseUid: String? = null
)





