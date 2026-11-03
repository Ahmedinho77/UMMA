package com.umma.app.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.umma.app.R
import com.umma.app.data.local.AppDatabase
import com.umma.app.data.model.AssignmentEntity
import com.umma.app.data.model.CourseEntity
import com.umma.app.data.model.HelpDeskTicketEntity
import com.umma.app.data.model.NewsEntity
import com.umma.app.data.model.ResultEntity
import com.umma.app.data.model.UserEntity
import com.umma.app.data.model.UserRole
import com.umma.app.databinding.ActivityListViewBinding
import com.umma.app.ui.adapters.GenericAdapter
import com.umma.app.ui.adapters.ListItem
import com.umma.app.util.Hash
import com.umma.app.util.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListViewBinding
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private var adapter: GenericAdapter? = null
    private var allItems: List<ListItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.get(this)
        session = SessionManager(this)
        val user = session.getUser()

        binding.toolbar.setNavigationOnClickListener { finish() }

        val type = intent.getStringExtra("TYPE") ?: return
        setupTitleAndFab(type, user?.role)

        if (user != null) {
            loadData(type, user.registrationNo)
        }

        // Setup Real-time Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTitleAndFab(type: String, role: UserRole?) {
        val titleText = when (type) {
            "COURSES" -> "Course & Unit Registration"
            "TIMETABLE" -> "Timetable & Examination"
            "ASSIGNMENTS" -> "Assignments & E-Learning"
            "RESULTS" -> "Examination Results & Transcript"
            "FEES" -> "Fees & Financial Management"
            "LIBRARY" -> "Library Management Catalog"
            "CAMPUS_SERVICES" -> "Campus Services & Facilities"
            "HELPDESK" -> "Help Desk & Welfare Support"
            "STUDENTS" -> "Registered Students"
            "USERS" -> "User & Staff Management"
            "NEWS" -> "University Announcements"
            "ENTER_MARKS" -> "Record Student Grades"
            "REPORTS" -> "System & Enrolment Reports"
            else -> type
        }
        binding.toolbar.title = titleText

        when {
            type == "USERS" && role == UserRole.ADMIN -> {
                binding.fabAdd.visibility = View.VISIBLE
                binding.fabAdd.text = "Add User"
                binding.fabAdd.setOnClickListener { showAddUserDialog() }
            }
            type == "COURSES" && role == UserRole.ADMIN -> {
                binding.fabAdd.visibility = View.VISIBLE
                binding.fabAdd.text = "Add Course"
                binding.fabAdd.setOnClickListener { showAddCourseDialog() }
            }
            type == "NEWS" && (role == UserRole.ADMIN || role == UserRole.STAFF) -> {
                binding.fabAdd.visibility = View.VISIBLE
                binding.fabAdd.text = "Publish News"
                binding.fabAdd.setOnClickListener { showAddNewsDialog() }
            }
            type == "ENTER_MARKS" && (role == UserRole.LECTURER || role == UserRole.ADMIN) -> {
                binding.fabAdd.visibility = View.VISIBLE
                binding.fabAdd.text = "Record Grade"
                binding.fabAdd.setOnClickListener { showEnterMarksDialog() }
            }
            type == "HELPDESK" -> {
                binding.fabAdd.visibility = View.VISIBLE
                binding.fabAdd.text = "Submit Ticket"
                binding.fabAdd.setOnClickListener { showSubmitTicketDialog() }
            }
            else -> {
                binding.fabAdd.visibility = View.GONE
            }
        }
    }

    private fun loadData(type: String, regNo: String) {
        lifecycleScope.launch {
            val items = mutableListOf<ListItem>()
            val user = session.getUser()

            when (type) {
                "COURSES" -> {
                    val courses = db.courseDao().getAllCourses().first()
                    courses.forEach { course ->
                        val onDelete: (() -> Unit)? = if (user?.role == UserRole.ADMIN) {
                            { deleteCourse(course.code, type, regNo) }
                        } else null

                        items.add(
                            ListItem(
                                rawId = course.code,
                                title = course.code,
                                subtitle = course.title,
                                extra = course.description,
                                badgeText = "STATUS: ACTIVE",
                                onDeleteClick = onDelete
                            )
                        )
                    }
                }
                "TIMETABLE" -> {
                    val timetables = db.timetableDao().getAllTimetables().first()
                    timetables.forEach { item ->
                        items.add(
                            ListItem(
                                title = "${item.unitCode}: ${item.title}",
                                subtitle = "Date/Time: ${item.dayOrDate}",
                                extra = "Venue: ${item.venue}",
                                badgeText = item.type,
                                actionText = if (item.type == "EXAM") "View Exam Card" else null,
                                onActionClick = if (item.type == "EXAM") {
                                    { showExamCardDialog(item.unitCode, item.title, item.dayOrDate, item.venue) }
                                } else null
                            )
                        )
                    }
                }
                "ASSIGNMENTS" -> {
                    val assignments = db.assignmentDao().getAllAssignments().first()
                    assignments.forEach { assign ->
                        val actionText = if (!assign.isSubmitted) "Upload Solution" else null
                        val onAction: (() -> Unit)? = if (!assign.isSubmitted) {
                            { showSubmitAssignmentDialog(assign) }
                        } else null

                        items.add(
                            ListItem(
                                id = assign.id,
                                title = "${assign.unitCode}: ${assign.title}",
                                subtitle = "Deadline: ${assign.deadline} | Grade: ${assign.marksAwarded}",
                                extra = assign.description,
                                badgeText = if (assign.isSubmitted) "SUBMITTED" else "PENDING",
                                actionText = actionText,
                                onActionClick = onAction
                            )
                        )
                    }
                }
                "RESULTS" -> {
                    val results = db.resultDao().getResultsForStudent(regNo).first()
                    results.forEach {
                        items.add(
                            ListItem(
                                title = "Unit: ${it.unitCode}",
                                subtitle = "Student Reg: ${it.studentRegNo}",
                                extra = "Marks Score: ${it.marks}/100",
                                grade = it.grade,
                                actionText = "View Transcript",
                                onActionClick = { showTranscriptDialog() }
                            )
                        )
                    }
                }
                "ENTER_MARKS" -> {
                    val allResults = db.resultDao().getAllResults().first()
                    allResults.forEach {
                        items.add(
                            ListItem(
                                title = "Unit: ${it.unitCode}",
                                subtitle = "Student: ${it.studentRegNo}",
                                extra = "Score: ${it.marks}/100",
                                grade = it.grade
                            )
                        )
                    }
                }
                "FEES" -> {
                    val fees = db.feeDao().getFeesForStudent(regNo).first()
                    fees.forEach { fee ->
                        val actionText = if (!fee.isPaid) "Pay Now" else "View Receipt"
                        val onAction: (() -> Unit) = if (!fee.isPaid) {
                            { payFee(fee.id, fee.description, fee.amount, regNo) }
                        } else {
                            { showReceiptDialog(fee.description, fee.amount) }
                        }

                        items.add(
                            ListItem(
                                id = fee.id,
                                title = fee.description,
                                subtitle = "Amount: KES ${fee.amount.toInt()}",
                                extra = if (fee.isPaid) "Transaction Complete" else "Payment Pending",
                                isPaid = fee.isPaid,
                                actionText = actionText,
                                onActionClick = onAction
                            )
                        )
                    }
                }
                "LIBRARY" -> {
                    val books = db.libraryBookDao().getAllBooks().first()
                    books.forEach { book ->
                        items.add(
                            ListItem(
                                id = book.id,
                                title = book.title,
                                subtitle = "Author: ${book.author} | ISBN: ${book.isbn}",
                                extra = "Category: ${book.category}" + (if (book.fineAmount > 0) " | Fine: KES ${book.fineAmount.toInt()}" else ""),
                                badgeText = if (book.isAvailable) "AVAILABLE" else "BORROWED",
                                actionText = if (book.isAvailable) "Reserve Book" else null,
                                onActionClick = if (book.isAvailable) {
                                    { reserveBook(book.id, book.title) }
                                } else null
                            )
                        )
                    }
                }
                "CAMPUS_SERVICES" -> {
                    val services = db.campusServiceDao().getAllCampusServices().first()
                    services.forEach { s ->
                        items.add(
                            ListItem(
                                title = "[${s.category}] ${s.title}",
                                subtitle = s.details,
                                extra = s.statusOrInfo,
                                actionText = "Apply / Info",
                                onActionClick = {
                                    Toast.makeText(this@ListActivity, "Service request received for ${s.title}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        )
                    }
                }
                "HELPDESK" -> {
                    val tickets = if (user?.role == UserRole.STUDENT) {
                        db.helpDeskTicketDao().getTicketsForStudent(regNo).first()
                    } else {
                        db.helpDeskTicketDao().getAllTickets().first()
                    }

                    tickets.forEach { ticket ->
                        items.add(
                            ListItem(
                                id = ticket.id,
                                title = "[${ticket.category}] ${ticket.subject}",
                                subtitle = "Student: ${ticket.studentRegNo}",
                                extra = ticket.description,
                                badgeText = ticket.status
                            )
                        )
                    }
                }
                "STUDENTS" -> {
                    val students = db.userDao().getAllStudents().first()
                    students.forEach {
                        items.add(
                            ListItem(
                                title = it.fullName,
                                subtitle = "Reg: ${it.registrationNo}",
                                extra = "Dept: ${it.department} | Phone: ${it.phone}"
                            )
                        )
                    }
                }
                "USERS" -> {
                    val users = db.userDao().getAllUsers().first()
                    users.forEach { u ->
                        val onDelete: (() -> Unit)? = if (user?.role == UserRole.ADMIN && u.email != user.email) {
                            { deleteUser(u.id, type, regNo) }
                        } else null

                        items.add(
                            ListItem(
                                id = u.id,
                                title = u.fullName,
                                subtitle = "Email: ${u.email} | ID: ${u.registrationNo}",
                                extra = "Dept: ${u.department} | Phone: ${u.phone}",
                                badgeText = u.role,
                                onDeleteClick = onDelete
                            )
                        )
                    }
                }
                "NEWS" -> {
                    val currentRole = session.getUser()?.role?.name ?: "STUDENT"
                    val news = db.newsDao().getNewsForRole(currentRole).first()
                    news.forEach {
                        items.add(
                            ListItem(
                                title = it.title,
                                subtitle = "Target Audience: ${it.targetRole} | Posted: ${it.date}",
                                extra = it.content
                            )
                        )
                    }
                }
                "REPORTS" -> {
                    val userCount = db.userDao().getUserCount()
                    val studentCount = db.userDao().getStudentCount()
                    val courseCount = db.courseDao().getCourseCount()

                    items.add(ListItem(title = "Total System Users", subtitle = "Registered accounts across all roles", extra = "$userCount Users"))
                    items.add(ListItem(title = "Enrolled Students", subtitle = "Active student portal profiles", extra = "$studentCount Students"))
                    items.add(ListItem(title = "Offered Courses & Units", subtitle = "Active academic curriculum units", extra = "$courseCount Courses"))
                    items.add(ListItem(title = "Fee Collection Status", subtitle = "Tuition payment summary", extra = "85% Fees Collected"))
                }
            }

            allItems = items
            updateUIState(allItems)
        }
    }

    private fun updateUIState(items: List<ListItem>) {
        if (items.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE

            if (adapter == null) {
                binding.recyclerView.layoutManager = LinearLayoutManager(this@ListActivity)
                adapter = GenericAdapter(items)
                binding.recyclerView.adapter = adapter
            } else {
                adapter?.updateItems(items)
            }
        }
    }

    private fun filterData(query: String) {
        if (query.isEmpty()) {
            updateUIState(allItems)
            return
        }
        val filtered = allItems.filter { item ->
            item.title.contains(query, ignoreCase = true) ||
                    item.subtitle.contains(query, ignoreCase = true) ||
                    item.extra.contains(query, ignoreCase = true) ||
                    (item.badgeText?.contains(query, ignoreCase = true) == true)
        }
        updateUIState(filtered)
    }

    private fun showExamCardDialog(unitCode: String, title: String, date: String, venue: String) {
        val user = session.getUser() ?: return
        val message = """
            ===============================
            UMMA UNIVERSITY DIGITAL EXAM CARD
            ===============================
            Student Name: ${user.fullName}
            Registration No: ${user.registrationNo}
            Course Unit: $unitCode - $title
            Date & Time: $date
            Venue: $venue
            
            Exam Rules:
            1. Present this digital card & Student ID at the entrance.
            2. No unauthorized electronic devices inside venue.
            3. Doors close 15 minutes after start time.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("🎫 Digital Examination Card")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showTranscriptDialog() {
        val user = session.getUser() ?: return
        val message = """
            ===============================
            OFFICIAL ACADEMIC TRANSCRIPT
            ===============================
            Name: ${user.fullName}
            Reg No: ${user.registrationNo}
            Department: ${user.department}
            
            Completed Units:
            - CS101: Introduction to Programming (85% - Grade A)
            - CS102: Data Structures & Algorithms (78% - Grade B)
            - CS201: Mobile App Development (92% - Grade A)
            
            Cumulative GPA: 3.8 / 4.0
            Academic Standing: GOOD STANDING / PASS
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("📜 Academic Transcript")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showReceiptDialog(desc: String, amount: Double) {
        val user = session.getUser() ?: return
        val message = """
            ===============================
            PAYMENT RECEIPT
            ===============================
            Receipt No: REC-2024-${(1000..9999).random()}
            Payer Name: ${user.fullName}
            Reg No: ${user.registrationNo}
            Description: $desc
            Amount Paid: KES ${amount.toInt()}
            Status: CLEARED & CONFIRMED
            Date: Today
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("🧾 Official Receipt")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showSubmitAssignmentDialog(assignment: AssignmentEntity) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_submit_assignment, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvAssignmentTitle)
        tvTitle.text = "Submit Solution for ${assignment.title}"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Upload Assignment Solution")
            .setView(dialogView)
            .setPositiveButton("Submit Solution", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val btnSubmit = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSubmit.setOnClickListener {
                lifecycleScope.launch {
                    db.assignmentDao().submitAssignment(assignment.id)
                    Toast.makeText(this@ListActivity, "Assignment Solution Submitted Successfully!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                    val user = session.getUser()
                    if (user != null) loadData("ASSIGNMENTS", user.registrationNo)
                }
            }
        }
        dialog.show()
    }

    private fun showSubmitTicketDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_submit_ticket, null)
        val etCat = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.etTicketCategory)
        val categories = arrayOf("IT / Portal Support", "Finance & Fees", "Academics & Exams", "Hostel & Accommodation", "General Welfare")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        etCat.setAdapter(adapter)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Submit Helpdesk Ticket")
            .setView(dialogView)
            .setPositiveButton("Submit Ticket", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val btnSubmit = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSubmit.setOnClickListener {
                val cat = etCat.text.toString().trim()
                val subject = dialogView.findViewById<TextInputEditText>(R.id.etTicketSubject)?.text.toString().trim() ?: ""
                val details = dialogView.findViewById<TextInputEditText>(R.id.etTicketDetails)?.text.toString().trim() ?: ""

                val user = session.getUser() ?: return@setOnClickListener

                if (cat.isNotEmpty() && subject.isNotEmpty() && details.isNotEmpty()) {
                    lifecycleScope.launch {
                        val ticket = HelpDeskTicketEntity(
                            studentRegNo = user.registrationNo,
                            category = cat,
                            subject = subject,
                            description = details,
                            status = "OPEN"
                        )
                        db.helpDeskTicketDao().insertTicket(ticket)
                        Toast.makeText(this@ListActivity, "Helpdesk Ticket Submitted! Ticket ID: #${System.currentTimeMillis() % 10000}", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                        loadData("HELPDESK", user.registrationNo)
                    }
                } else {
                    Toast.makeText(this@ListActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun reserveBook(bookId: Long, bookTitle: String) {
        lifecycleScope.launch {
            db.libraryBookDao().updateBookAvailability(bookId, false)
            Toast.makeText(this@ListActivity, "Reserved '$bookTitle'. Please pick up at the main library desk.", Toast.LENGTH_LONG).show()
            val user = session.getUser()
            if (user != null) loadData("LIBRARY", user.registrationNo)
        }
    }

    private fun payFee(feeId: Long, description: String, amount: Double, regNo: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Fee Payment")
            .setMessage("Are you sure you want to pay KES ${amount.toInt()} for '$description'?")
            .setPositiveButton("Pay Now") { _, _ ->
                lifecycleScope.launch {
                    db.feeDao().updateFeeStatus(feeId, true)
                    Toast.makeText(this@ListActivity, "Payment Successful! Receipt Generated.", Toast.LENGTH_SHORT).show()
                    loadData("FEES", regNo)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUser(userId: Long, type: String, regNo: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete User Account")
            .setMessage("Are you sure you want to delete this account?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    db.userDao().deleteUserById(userId)
                    Toast.makeText(this@ListActivity, "User deleted successfully", Toast.LENGTH_SHORT).show()
                    loadData(type, regNo)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCourse(courseCode: String, type: String, regNo: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete course '$courseCode'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    db.courseDao().deleteCourse(courseCode)
                    Toast.makeText(this@ListActivity, "Course deleted successfully", Toast.LENGTH_SHORT).show()
                    loadData(type, regNo)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)

        val etRole = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.etRole)
        val roles = arrayOf("STUDENT", "LECTURER", "STAFF", "ADMIN")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        etRole.setAdapter(adapter)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New User")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
                val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
                val etReg = dialogView.findViewById<TextInputEditText>(R.id.etReg)

                val name = etName?.text.toString().trim()
                val email = etEmail?.text.toString().trim()
                val role = etRole?.text.toString().trim().uppercase()
                val regNo = etReg?.text.toString().trim()

                if (name.isNotEmpty() && email.isNotEmpty() && regNo.isNotEmpty() && (role in roles)) {
                    lifecycleScope.launch {
                        val newUser = UserEntity(
                            fullName = name,
                            email = email,
                            passwordHash = Hash.sha256("password123"),
                            role = role,
                            registrationNo = regNo,
                            department = if (role == "STUDENT") "Computer Science" else "Faculty"
                        )
                        db.userDao().insertUser(newUser)
                        Toast.makeText(this@ListActivity, "User Added! Default Password: password123", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                        loadData("USERS", "")
                    }
                } else {
                    Toast.makeText(this@ListActivity, "Please complete all fields correctly", Toast.LENGTH_LONG).show()
                }
            }
        }
        dialog.show()
    }

    private fun showAddCourseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_course, null)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Course")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                val etCode = dialogView.findViewById<TextInputEditText>(R.id.etCourseCode)
                val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etCourseTitle)
                val etDesc = dialogView.findViewById<TextInputEditText>(R.id.etCourseDesc)

                val code = etCode?.text.toString().trim().uppercase()
                val title = etTitle?.text.toString().trim()
                val desc = etDesc?.text.toString().trim()

                if (code.isNotEmpty() && title.isNotEmpty()) {
                    lifecycleScope.launch {
                        val newCourse = CourseEntity(code = code, title = title, description = desc)
                        db.courseDao().insertCourse(newCourse)
                        Toast.makeText(this@ListActivity, "Course Added Successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadData("COURSES", "")
                    }
                } else {
                    Toast.makeText(this@ListActivity, "Course Code & Title are required", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun showEnterMarksDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_enter_marks, null)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Record Student Grade")
            .setView(dialogView)
            .setPositiveButton("Save Grade", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                val etReg = dialogView.findViewById<TextInputEditText>(R.id.etStudentReg)
                val etCode = dialogView.findViewById<TextInputEditText>(R.id.etUnitCode)
                val etMarks = dialogView.findViewById<TextInputEditText>(R.id.etMarks)

                val reg = etReg?.text.toString().trim()
                val code = etCode?.text.toString().trim().uppercase()
                val marksStr = etMarks?.text.toString().trim()

                if (reg.isNotEmpty() && code.isNotEmpty() && marksStr.isNotEmpty()) {
                    val marks = marksStr.toIntOrNull() ?: 0
                    val grade = when {
                        marks >= 80 -> "A"
                        marks >= 70 -> "B"
                        marks >= 60 -> "C"
                        marks >= 50 -> "D"
                        else -> "F"
                    }

                    lifecycleScope.launch {
                        val result = ResultEntity(studentRegNo = reg, unitCode = code, marks = marks, grade = grade)
                        db.resultDao().insertResult(result)
                        Toast.makeText(this@ListActivity, "Marks Saved! Grade: $grade", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadData("ENTER_MARKS", reg)
                    }
                } else {
                    Toast.makeText(this@ListActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun showAddNewsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_news, null)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Publish News")
            .setView(dialogView)
            .setPositiveButton("Next", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val btnNext = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnNext.setOnClickListener {
                val title = dialogView.findViewById<TextInputEditText>(R.id.etNewsTitle)?.text?.toString()?.trim() ?: ""
                val content = dialogView.findViewById<TextInputEditText>(R.id.etNewsContent)?.text?.toString()?.trim() ?: ""

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    dialog.dismiss()
                    showChooseRoleDialog(title, content)
                } else {
                    Toast.makeText(this@ListActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun showChooseRoleDialog(title: String, content: String) {
        val roles = arrayOf("ALL", "STUDENT", "LECTURER", "STAFF")

        AlertDialog.Builder(this)
            .setTitle("Who is this news for?")
            .setItems(roles) { _, which ->
                val selectedRole = roles[which]
                lifecycleScope.launch {
                    val newNews = NewsEntity(
                        title = title,
                        content = content,
                        date = "Today",
                        targetRole = selectedRole
                    )
                    db.newsDao().insertNews(listOf(newNews))
                    Toast.makeText(this@ListActivity, "News Published for $selectedRole!", Toast.LENGTH_SHORT).show()
                    loadData("NEWS", "")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}