package com.arianesanga.event.data.repository

import com.arianesanga.event.data.local.dao.UserDao
import com.arianesanga.event.data.local.model.User
import com.arianesanga.event.data.remote.repository.UserRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val local: UserDao,
    private val remote: UserRemoteRepository
) {

    // ✅ Obtém usuário local
    suspend fun getUser(uid: String): User? = withContext(Dispatchers.IO) {
        local.getUserByUid(uid)
    }

    // ✅ Busca usuário remoto e salva localmente
    suspend fun fetchRemoteUser(uid: String): User? = withContext(Dispatchers.IO) {
        val data = remote.getUser(uid)
        data?.let {
            val user = User(
                uid = uid,
                fullname = it["fullname"] as? String ?: "",
                username = it["username"] as? String ?: "",
                email = it["email"] as? String ?: "",
                phone = it["phone"] as? String ?: "",
                photoUri = it["photoUri"] as? String
            )
            local.insertUser(user)
            user
        }
    }

    // ✅ Criação (local + remoto)
    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        local.insertUser(user)
        remote.createUser(user.uid, user.toMap())
    }

    // ✅ Atualização (local + remoto)
    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        local.updateUser(user)
        remote.updateUser(user.uid, user.toMap())
    }

    // ✅ Exclusão (local + remoto)
    suspend fun deleteUser(user: User) = withContext(Dispatchers.IO) {
        local.deleteUser(user)
        remote.deleteUser(user.uid)
    }

    // Conversão User → Firestore
    private fun User.toMap(): Map<String, Any> = mapOf(
        "fullname" to fullname,
        "username" to username,
        "email" to email,
        "phone" to phone,
        "photoUri" to photoUri
    ).filterValues { it != null } as Map<String, Any>
}