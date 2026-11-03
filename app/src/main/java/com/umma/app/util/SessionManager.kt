package com.umma.app.util

import android.content.Context
import com.umma.app.data.model.SessionUser
import com.umma.app.data.model.UserRole

class SessionManager(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("umma_session", Context.MODE_PRIVATE)

    fun save(user: SessionUser) {
        prefs.edit()
            .putLong("id", user.id)
            .putString("name", user.fullName)
            .putString("email", user.email)
            .putString("role", user.role.name)
            .putString("reg", user.registrationNo)
            .putString("dept", user.department)
            .putString("photo", user.photoUri)
            .putString("phone", user.phone)
            .putString("contact", user.contactInfo)
            .apply()
    }

    fun getUser(): SessionUser? {
        val id = prefs.getLong("id", -1L)
        if (id == -1L) return null
        return SessionUser(
            id = id,
            fullName = prefs.getString("name", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            role = runCatching { UserRole.valueOf(prefs.getString("role", "STUDENT")!!) }
                .getOrDefault(UserRole.STUDENT),
            registrationNo = prefs.getString("reg", "") ?: "",
            department = prefs.getString("dept", "") ?: "",
            photoUri = prefs.getString("photo", null),
            phone = prefs.getString("phone", "+254 700 000000") ?: "+254 700 000000",
            contactInfo = prefs.getString("contact", "Main Campus, Garissa Road, Thika") ?: "Main Campus, Garissa Road, Thika"
        )
    }

    fun isLoggedIn() = getUser() != null
    fun logout() = prefs.edit().clear().apply()
    fun updateProfilePhoto(uri: String) {
        prefs.edit().putString("photo", uri).apply()
    }
    fun updateName(newName: String) {
        prefs.edit().putString("name", newName).apply()
    }
}