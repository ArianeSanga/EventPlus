package com.arianesanga.event.data

class EventoRepository(private val eventoDao: EventoDao){


    //função que inseri um evento no banco
    suspend fun insert(evento: Evento){
        eventoDao.inserir(evento)
    }


    //função que busca todos os eventos
    suspend fun getAllEventos(): List<Evento>{
        return eventoDao.listarTodos()
    }
}