package com.umma.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.umma.app.data.local.dao.*
import com.umma.app.data.model.*
import com.umma.app.util.Hash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        ResultEntity::class,
        FeeStatementEntity::class,
        NewsEntity::class,
        TimetableEntity::class,
        AssignmentEntity::class,
        LibraryBookEntity::class,
        HelpDeskTicketEntity::class,
        CampusServiceEntity::class,
        AttendanceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun resultDao(): ResultDao
    abstract fun feeDao(): FeeDao
    abstract fun newsDao(): NewsDao
    abstract fun timetableDao(): TimetableDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun libraryBookDao(): LibraryBookDao
    abstract fun helpDeskTicketDao(): HelpDeskTicketDao
    abstract fun campusServiceDao(): CampusServiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "umma_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                // Seed data on first run
                CoroutineScope(Dispatchers.IO).launch {
                    seedData(instance)
                }
                instance
            }
        }

        private suspend fun seedData(db: AppDatabase) {
            val userDao = db.userDao()
            if (userDao.getUserByEmail("admin@umma.ac.ke") == null) {

                // 1. Seed Users
                userDao.insertUser(
                    UserEntity(
                        fullName = "Ahmed Ali Osman",
                        email = "ahmed@umma.ac.ke",
                        passwordHash = Hash.sha256("password123"),
                        role = "STUDENT",
                        registrationNo = "BSCS/2024/52881",
                        department = "Computer Science",
                        phone = "+254 712 345678"
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        fullName = "Dr. Fatima Hassan",
                        email = "fatima@umma.ac.ke",
                        passwordHash = Hash.sha256("password123"),
                        role = "LECTURER",
                        registrationNo = "LEC/2019/0042",
                        department = "Computer Science",
                        phone = "+254 722 987654"
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        fullName = "Grace Wanjiku",
                        email = "staff@umma.ac.ke",
                        passwordHash = Hash.sha256("password123"),
                        role = "STAFF",
                        registrationNo = "STF/2021/0105",
                        department = "Student Welfare & Helpdesk",
                        phone = "+254 733 112233"
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        fullName = "Admin User",
                        email = "admin@umma.ac.ke",
                        passwordHash = Hash.sha256("admin123"),
                        role = "ADMIN",
                        registrationNo = "ADM/2020/001",
                        department = "Administration",
                        phone = "+254 700 000000"
                    )
                )

                // 2. Seed Courses
                db.courseDao().insertCourses(listOf(
                    CourseEntity("CS101", "Introduction to Programming", "Basics of Kotlin and Java algorithms."),
                    CourseEntity("CS102", "Data Structures & Algorithms", "Lists, Trees, Graphs, and Complexity Analysis."),
                    CourseEntity("CS201", "Mobile App Development", "Native Android development with Kotlin & Jetpack."),
                    CourseEntity("CS202", "Database Systems", "Relational Databases, SQL, and Room ORM."),
                    CourseEntity("CS301", "Software Engineering", "Agile methodologies, UML design, and testing.")
                ))

                // 3. Seed Results
                db.resultDao().insertResults(listOf(
                    ResultEntity(studentRegNo = "BSCS/2024/52881", unitCode = "CS101", marks = 85, grade = "A"),
                    ResultEntity(studentRegNo = "BSCS/2024/52881", unitCode = "CS102", marks = 78, grade = "B"),
                    ResultEntity(studentRegNo = "BSCS/2024/52881", unitCode = "CS201", marks = 92, grade = "A")
                ))

                // 4. Seed Fees
                db.feeDao().insertFees(listOf(
                    FeeStatementEntity(studentRegNo = "BSCS/2024/52881", description = "Semester 1 Tuition Fee", amount = 50000.0, isPaid = true),
                    FeeStatementEntity(studentRegNo = "BSCS/2024/52881", description = "Semester 2 Tuition Fee", amount = 50000.0, isPaid = false),
                    FeeStatementEntity(studentRegNo = "BSCS/2024/52881", description = "Library & Lab Activity Fee", amount = 5000.0, isPaid = true)
                ))

                // 5. Seed News
                db.newsDao().insertNews(listOf(
                    NewsEntity(
                        title = "Welcome to Academic Year 2024/2025",
                        content = "Semester 2 lectures begin on Monday. Please ensure your unit registration is complete.",
                        date = "Today",
                        targetRole = "ALL"
                    ),
                    NewsEntity(
                        title = "Fee Clearance & Exam Cards",
                        content = "All tuition fees must be cleared by month end to generate examination cards.",
                        date = "Yesterday",
                        targetRole = "STUDENT"
                    ),
                    NewsEntity(
                        title = "Continuous Assessment Marks Submission",
                        content = "Lecturers are reminded to upload CAT 1 and CAT 2 marks into the portal.",
                        date = "3 days ago",
                        targetRole = "LECTURER"
                    )
                ))

                // 6. Seed Timetables
                db.timetableDao().insertTimetables(listOf(
                    TimetableEntity(unitCode = "CS201", title = "Mobile App Development", dayOrDate = "Monday 08:00 AM - 11:00 AM", venue = "Lab 3, Science Block", type = "LECTURE"),
                    TimetableEntity(unitCode = "CS102", title = "Data Structures", dayOrDate = "Wednesday 11:00 AM - 02:00 PM", venue = "LH 04, Main Complex", type = "LECTURE"),
                    TimetableEntity(unitCode = "CS201", title = "CS201 Final Exam", dayOrDate = "15th Nov 09:00 AM", venue = "Auditorium Hall A", type = "EXAM"),
                    TimetableEntity(unitCode = "CS101", title = "CS101 Final Exam", dayOrDate = "18th Nov 02:00 PM", venue = "Auditorium Hall B", type = "EXAM")
                ))

                // 7. Seed Assignments
                db.assignmentDao().insertAssignments(listOf(
                    AssignmentEntity(unitCode = "CS201", title = "Kotlin Android UI App", deadline = "Friday 11:59 PM", description = "Develop a responsive Android app with Room DB and ViewBinding.", isSubmitted = false, marksAwarded = "Pending"),
                    AssignmentEntity(unitCode = "CS102", title = "Binary Search Tree Implementation", deadline = "Next Monday", description = "Implement BST in Kotlin with in-order and post-order traversal.", isSubmitted = true, marksAwarded = "18/20")
                ))

                // 8. Seed Library Books
                db.libraryBookDao().insertBooks(listOf(
                    LibraryBookEntity(title = "Android Programming: The Big Nerd Ranch Guide", author = "Bill Phillips", isbn = "978-0135245125", category = "Computer Science", isAvailable = true, fineAmount = 0.0),
                    LibraryBookEntity(title = "Kotlin in Action", author = "Dmitry Jemerov", isbn = "978-1617293290", category = "Computer Science", isAvailable = false, fineAmount = 150.0),
                    LibraryBookEntity(title = "Introduction to Algorithms (CLRS)", author = "Thomas Cormen", isbn = "978-0262033848", category = "Mathematics & CS", isAvailable = true, fineAmount = 0.0)
                ))

                // 9. Seed Campus Services
                db.campusServiceDao().insertCampusServices(listOf(
                    CampusServiceEntity(category = "HOSTEL", title = "Block A - Male Residence", details = "Furnished rooms, high-speed WiFi, laundry facilities.", statusOrInfo = "Rooms Available (KES 12,000/sem)"),
                    CampusServiceEntity(category = "CAFETERIA", title = "Main Campus Dining Hall", details = "Serving breakfast, lunch, and dinner daily.", statusOrInfo = "Open 06:30 AM - 09:00 PM"),
                    CampusServiceEntity(category = "TRANSPORT", title = "Campus Shuttle Service", details = "Regular shuttle runs between Thika Town & Main Campus.", statusOrInfo = "Schedule: Every 30 mins"),
                    CampusServiceEntity(category = "HEALTH", title = "University Health Centre", details = "24/7 emergency care, clinical services, and counseling.", statusOrInfo = "Emergency Line: Ext. 999"),
                    CampusServiceEntity(category = "CAREER", title = "Career & Alumni Placement", details = "Internship openings, resume reviews, and networking events.", statusOrInfo = "3 Active Internships Open")
                ))

                // 10. Seed HelpDesk Ticket
                db.helpDeskTicketDao().insertTicket(
                    HelpDeskTicketEntity(studentRegNo = "BSCS/2024/52881", category = "IT / Portal Support", subject = "Exam Card Generation Issue", description = "Unable to download exam card after fee payment.", status = "IN_PROGRESS")
                )
            }
        }
    }
}