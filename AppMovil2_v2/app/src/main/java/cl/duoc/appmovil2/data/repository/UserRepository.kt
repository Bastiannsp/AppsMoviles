package cl.duoc.appmovil2.data.repository

import cl.duoc.appmovil2.data.local.dao.UserDao
import cl.duoc.appmovil2.data.local.entity.User

class UserRepository(private val userDao: UserDao) {

    suspend fun isEmailRegistered(email: String): Boolean {
        if (email.isBlank()) return false
        return userDao.getUserByEmail(email.trim()) != null
    }

    suspend fun registerUser(user: User) {
        userDao.insert(user)
    }
}
