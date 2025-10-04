package com.arianesanga.event.data

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
