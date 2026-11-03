package com.umma.app.data.model

enum class UserRole { STUDENT, LECTURER, ADMIN, STAFF }

data class SessionUser(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val registrationNo: String,
    val department: String,
    val photoUri: String?,
    val phone: String = "",
    val contactInfo: String = ""
)