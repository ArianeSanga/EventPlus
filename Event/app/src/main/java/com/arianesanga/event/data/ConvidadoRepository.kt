package com.arianesanga.event.data

class ConvidadoRepository(private val convidadoDao: ConvidadoDao) {

    // Insere um convidado no banco
    suspend fun inserir(convidado: Convidado) {
        convidadoDao.inserir(convidado)
    }


    fun listarPorEvento(eventoId: Int) = convidadoDao.listarPorEvento(eventoId)


    suspend fun sincronizarConvidado(convidado: Convidado) {

        val uid = convidado.firebaseUid
        if (uid != null) {
            val convidadoExistente = convidadoDao.buscarPorFirebaseUid(uid)

            if (convidadoExistente != null) {
                // 2. Se existe, atualiza os dados (nome, telefone, etc.) mantendo o ID local original
                val convidadoAtualizado = convidado.copy(id = convidadoExistente.id)
                convidadoDao.atualizar(convidadoAtualizado)
            } else {
                // 3. Se não existe, insere como novo
                convidadoDao.inserir(convidado)
            }
        } else {
            // Caso o convidado não tenha UID
            convidadoDao.inserir(convidado)
        }
    }
}