package com.arianesanga.event.data.local.repository

import com.arianesanga.event.data.local.dao.UserDao
import com.arianesanga.event.data.local.model.User

class LocalUserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: User) = userDao.insert(user)
    suspend fun updateUser(user: User) = userDao.update(user)
    suspend fun deleteUser(user: User) = userDao.delete(user)
    suspend fun getUser(uid: String) = userDao.getUser(uid)
}