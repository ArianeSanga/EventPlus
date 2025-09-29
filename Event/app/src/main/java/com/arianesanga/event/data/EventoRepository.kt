package com.arianesanga.event.data

class EventoRepository(private val eventoDao: EventoDao) {

    suspend fun insert(evento: Evento) {
        eventoDao.inserir(evento)
    }

    suspend fun getAllEventos(): List<Evento> {
        return eventoDao.listarTodos()
    }

    suspend fun delete(evento: Evento) {
        eventoDao.deletar(evento)
    }
}
