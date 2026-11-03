package com.umma.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val role: String, // "STUDENT", "LECTURER", "ADMIN", "STAFF"
    val registrationNo: String,
    val department: String,
    val photoUri: String? = null,
    val phone: String = "+254 700 000000",
    val contactInfo: String = "Main Campus, Garissa Road, Thika"
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val code: String,
    val title: String,
    val description: String,
    val isRegistered: Boolean = true
)

@Entity(tableName = "results")
data class ResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentRegNo: String,
    val unitCode: String,
    val marks: Int,
    val grade: String
)

@Entity(tableName = "fee_statements")
data class FeeStatementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentRegNo: String,
    val description: String,
    val amount: Double,
    val isPaid: Boolean
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val date: String,
    val targetRole: String
)

@Entity(tableName = "timetables")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitCode: String,
    val title: String,
    val dayOrDate: String,
    val venue: String,
    val type: String // "LECTURE" or "EXAM"
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitCode: String,
    val title: String,
    val deadline: String,
    val description: String,
    val isSubmitted: Boolean = false,
    val marksAwarded: String = "Pending"
)

@Entity(tableName = "library_books")
data class LibraryBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val isbn: String,
    val category: String,
    val isAvailable: Boolean = true,
    val fineAmount: Double = 0.0
)

@Entity(tableName = "helpdesk_tickets")
data class HelpDeskTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentRegNo: String,
    val category: String,
    val subject: String,
    val description: String,
    val status: String = "OPEN" // "OPEN", "IN_PROGRESS", "RESOLVED"
)

@Entity(tableName = "campus_services")
data class CampusServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "HOSTEL", "CAFETERIA", "TRANSPORT", "HEALTH", "CAREER"
    val title: String,
    val details: String,
    val statusOrInfo: String
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentRegNo: String,
    val unitCode: String,
    val date: String,
    val isPresent: Boolean = true
)