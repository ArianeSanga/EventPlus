package com.arianesanga.event.data.repository

import com.arianesanga.event.data.dao.EventoDao
import com.arianesanga.event.data.model.Evento

class EventoRepository(private val eventoDao: EventoDao) {

    suspend fun insert(evento: Evento) {
        eventoDao.inserir(evento)
    }

    suspend fun getEventosByOwner(ownerUid: String): List<Evento> {
        return eventoDao.listarPorOwner(ownerUid)
    }

    suspend fun delete(evento: Evento) {
        eventoDao.deletar(evento)
    }
}
