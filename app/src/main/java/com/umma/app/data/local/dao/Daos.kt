package com.umma.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.umma.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("UPDATE users SET fullName = :newName, passwordHash = :newPassword WHERE id = :userId")
    suspend fun updateUserProfile(userId: Long, newName: String, newPassword: String)

    @Query("UPDATE users SET fullName = :newName WHERE id = :userId")
    suspend fun updateUserNameOnly(userId: Long, newName: String)

    @Query("SELECT * FROM users WHERE role = 'STUDENT'")
    fun getAllStudents(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT COUNT(*) FROM users WHERE role = 'STUDENT'")
    suspend fun getStudentCount(): Int
}

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("DELETE FROM courses WHERE code = :code")
    suspend fun deleteCourse(code: String)

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int
}

@Dao
interface ResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ResultEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultEntity)

    @Query("SELECT * FROM results WHERE studentRegNo = :regNo")
    fun getResultsForStudent(regNo: String): Flow<List<ResultEntity>>

    @Query("SELECT * FROM results")
    fun getAllResults(): Flow<List<ResultEntity>>

    @Query("SELECT COUNT(*) FROM results WHERE studentRegNo = :regNo")
    suspend fun getResultsCountForStudent(regNo: String): Int
}

@Dao
interface FeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFees(fees: List<FeeStatementEntity>)

    @Query("SELECT * FROM fee_statements WHERE studentRegNo = :regNo")
    fun getFeesForStudent(regNo: String): Flow<List<FeeStatementEntity>>

    @Query("UPDATE fee_statements SET isPaid = :isPaid WHERE id = :id")
    suspend fun updateFeeStatus(id: Long, isPaid: Boolean)

    @Query("SELECT SUM(amount) FROM fee_statements WHERE studentRegNo = :regNo AND isPaid = 0")
    suspend fun getUnpaidFeeTotal(regNo: String): Double?
}

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: List<NewsEntity>)

    @Query("SELECT * FROM news WHERE targetRole = 'ALL' OR targetRole = :userRole ORDER BY id DESC")
    fun getNewsForRole(userRole: String): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE targetRole = 'ALL' OR targetRole = :userRole ORDER BY id DESC LIMIT 1")
    fun getLatestNewsForRole(userRole: String): Flow<NewsEntity?>
}

@Dao
interface TimetableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetables(timetables: List<TimetableEntity>)

    @Query("SELECT * FROM timetables")
    fun getAllTimetables(): Flow<List<TimetableEntity>>
}

@Dao
interface AssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Query("SELECT * FROM assignments")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("UPDATE assignments SET isSubmitted = 1 WHERE id = :id")
    suspend fun submitAssignment(id: Long)
}

@Dao
interface LibraryBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<LibraryBookEntity>)

    @Query("SELECT * FROM library_books")
    fun getAllBooks(): Flow<List<LibraryBookEntity>>

    @Query("UPDATE library_books SET isAvailable = :available WHERE id = :id")
    suspend fun updateBookAvailability(id: Long, available: Boolean)
}

@Dao
interface HelpDeskTicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: HelpDeskTicketEntity)

    @Query("SELECT * FROM helpdesk_tickets WHERE studentRegNo = :regNo ORDER BY id DESC")
    fun getTicketsForStudent(regNo: String): Flow<List<HelpDeskTicketEntity>>

    @Query("SELECT * FROM helpdesk_tickets ORDER BY id DESC")
    fun getAllTickets(): Flow<List<HelpDeskTicketEntity>>
}

@Dao
interface CampusServiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampusServices(services: List<CampusServiceEntity>)

    @Query("SELECT * FROM campus_services")
    fun getAllCampusServices(): Flow<List<CampusServiceEntity>>
}