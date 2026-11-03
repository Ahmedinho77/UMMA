package com.umma.app.data.repository

import android.content.Context
import com.umma.app.data.local.AppDatabase
import com.umma.app.data.model.SessionUser
import com.umma.app.data.model.UserEntity
import com.umma.app.data.model.UserRole
import com.umma.app.util.Hash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {
    private val userDao = AppDatabase.get(context).userDao()

    suspend fun login(email: String, password: String): Result<SessionUser> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserByEmail(email)
                ?: return@withContext Result.failure(Exception("User not found"))

            if (user.passwordHash == Hash.sha256(password)) {
                val sessionUser = SessionUser(
                    id = user.id,
                    fullName = user.fullName,
                    email = user.email,
                    role = runCatching { UserRole.valueOf(user.role) }.getOrDefault(UserRole.STUDENT),
                    registrationNo = user.registrationNo,
                    department = user.department,
                    photoUri = user.photoUri,
                    phone = user.phone,
                    contactInfo = user.contactInfo
                )
                Result.success(sessionUser)
            } else {
                Result.failure(Exception("Incorrect password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(user: UserEntity, rawPassword: String): Result<SessionUser> = withContext(Dispatchers.IO) {
        try {
            if (userDao.getUserByEmail(user.email) != null) {
                return@withContext Result.failure(Exception("Email already exists"))
            }

            val newUser = user.copy(passwordHash = Hash.sha256(rawPassword))
            val id = userDao.insertUser(newUser)

            val sessionUser = SessionUser(
                id = id,
                fullName = newUser.fullName,
                email = newUser.email,
                role = runCatching { UserRole.valueOf(newUser.role) }.getOrDefault(UserRole.STUDENT),
                registrationNo = newUser.registrationNo,
                department = newUser.department,
                photoUri = newUser.photoUri,
                phone = newUser.phone,
                contactInfo = newUser.contactInfo
            )
            Result.success(sessionUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}