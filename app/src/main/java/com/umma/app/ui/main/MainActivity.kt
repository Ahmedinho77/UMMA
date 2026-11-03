package com.umma.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.umma.app.R
import com.umma.app.data.local.AppDatabase
import com.umma.app.data.model.UserRole
import com.umma.app.databinding.ActivityMainBinding
import com.umma.app.ui.ListActivity
import com.umma.app.ui.ProfileActivity
import com.umma.app.ui.auth.AuthActivity
import com.umma.app.util.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.get(this)
        session = SessionManager(this)
        val user = session.getUser()

        if (user == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        // 1. Setup Header & User Info
        binding.tvUserName.text = user.fullName
        binding.tvRole.text = user.role.name
        binding.tvSubDetails.text = if (user.registrationNo.isNotEmpty()) user.registrationNo else user.department

        // Avatar
        if (!user.photoUri.isNullOrEmpty()) {
            binding.ivUserAvatar.load(user.photoUri) {
                crossfade(true)
                placeholder(R.drawable.ic_profile_placeholder)
                error(R.drawable.ic_profile_placeholder)
            }
        } else {
            binding.ivUserAvatar.setImageResource(R.drawable.ic_profile_placeholder)
        }

        binding.cardHeader.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // 2. Load Stats & Announcement Banner
        loadDashboardStats(user.role, user.registrationNo)
        observeLatestAnnouncement(user.role.name)

        // 3. Customize Dashboard based on Role
        when (user.role) {
            UserRole.STUDENT -> setupStudentDashboard()
            UserRole.LECTURER -> setupLecturerDashboard()
            UserRole.STAFF -> setupStaffDashboard()
            UserRole.ADMIN -> setupAdminDashboard()
        }

        // 4. Logout Logic
        binding.btnLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val user = session.getUser() ?: return
        binding.tvUserName.text = user.fullName
        if (!user.photoUri.isNullOrEmpty()) {
            binding.ivUserAvatar.load(user.photoUri)
        }
        loadDashboardStats(user.role, user.registrationNo)
    }

    private fun observeLatestAnnouncement(roleName: String) {
        lifecycleScope.launch {
            db.newsDao().getLatestNewsForRole(roleName).collectLatest { news ->
                if (news != null) {
                    binding.cardLatestNews.visibility = View.VISIBLE
                    binding.tvNewsTitle.text = news.title
                    binding.tvNewsSnippet.text = news.content
                    binding.tvNewsDate.text = news.date

                    val openNewsIntent = Intent(this@MainActivity, ListActivity::class.java).apply {
                        putExtra("TYPE", "NEWS")
                    }
                    binding.cardLatestNews.setOnClickListener { startActivity(openNewsIntent) }
                    binding.tvReadAllNews.setOnClickListener { startActivity(openNewsIntent) }
                } else {
                    binding.cardLatestNews.visibility = View.GONE
                }
            }
        }
    }

    private fun loadDashboardStats(role: UserRole, regNo: String) {
        lifecycleScope.launch {
            when (role) {
                UserRole.STUDENT -> {
                    val coursesCount = db.courseDao().getCourseCount()
                    val unpaidTotal = db.feeDao().getUnpaidFeeTotal(regNo) ?: 0.0

                    binding.tvStat1Label.text = "Courses"
                    binding.tvStat1Value.text = "$coursesCount"

                    binding.tvStat2Label.text = "GPA"
                    binding.tvStat2Value.text = "3.8"

                    binding.tvStat3Label.text = "Fee Balance"
                    binding.tvStat3Value.text = if (unpaidTotal > 0) "KES ${unpaidTotal.toInt()}" else "KES 0"
                }
                UserRole.LECTURER -> {
                    val studentCount = db.userDao().getStudentCount()
                    val courseCount = db.courseDao().getCourseCount()

                    binding.tvStat1Label.text = "Students"
                    binding.tvStat1Value.text = "$studentCount"

                    binding.tvStat2Label.text = "Courses"
                    binding.tvStat2Value.text = "$courseCount"

                    binding.tvStat3Label.text = "Status"
                    binding.tvStat3Value.text = "Active"
                }
                UserRole.STAFF -> {
                    val studentCount = db.userDao().getStudentCount()
                    val courseCount = db.courseDao().getCourseCount()

                    binding.tvStat1Label.text = "Students"
                    binding.tvStat1Value.text = "$studentCount"

                    binding.tvStat2Label.text = "Courses"
                    binding.tvStat2Value.text = "$courseCount"

                    binding.tvStat3Label.text = "Desk Status"
                    binding.tvStat3Value.text = "Online"
                }
                UserRole.ADMIN -> {
                    val userCount = db.userDao().getUserCount()
                    val studentCount = db.userDao().getStudentCount()
                    val courseCount = db.courseDao().getCourseCount()

                    binding.tvStat1Label.text = "Total Users"
                    binding.tvStat1Value.text = "$userCount"

                    binding.tvStat2Label.text = "Students"
                    binding.tvStat2Value.text = "$studentCount"

                    binding.tvStat3Label.text = "Courses"
                    binding.tvStat3Value.text = "$courseCount"
                }
            }
        }
    }

    private fun openType(type: String) {
        startActivity(Intent(this, ListActivity::class.java).putExtra("TYPE", type))
    }

    private fun showActionButtons(count: Int) {
        val buttons = listOf(
            binding.btnAction1,
            binding.btnAction2,
            binding.btnAction3,
            binding.btnAction4,
            binding.btnAction5,
            binding.btnAction6,
            binding.btnAction7,
            binding.btnAction8
        )
        buttons.forEachIndexed { index, button ->
            button.visibility = if (index < count) View.VISIBLE else View.GONE
        }
    }

    private fun setupStudentDashboard() {
        showActionButtons(8)

        binding.btnAction1.text = "Courses & Units"
        binding.btnAction1.setIconResource(R.drawable.ic_courses)
        binding.btnAction1.setOnClickListener { openType("COURSES") }

        binding.btnAction2.text = "Timetable & Exams"
        binding.btnAction2.setIconResource(R.drawable.ic_results)
        binding.btnAction2.setOnClickListener { openType("TIMETABLE") }

        binding.btnAction3.text = "Assignments"
        binding.btnAction3.setIconResource(R.drawable.ic_notes)
        binding.btnAction3.setOnClickListener { openType("ASSIGNMENTS") }

        binding.btnAction4.text = "Results & Transcript"
        binding.btnAction4.setIconResource(R.drawable.ic_marks)
        binding.btnAction4.setOnClickListener { openType("RESULTS") }

        binding.btnAction5.text = "Fee Statement"
        binding.btnAction5.setIconResource(R.drawable.ic_fees)
        binding.btnAction5.setOnClickListener { openType("FEES") }

        binding.btnAction6.text = "Library Books"
        binding.btnAction6.setIconResource(R.drawable.ic_courses)
        binding.btnAction6.setOnClickListener { openType("LIBRARY") }

        binding.btnAction7.text = "Campus Services"
        binding.btnAction7.setIconResource(R.drawable.ic_students)
        binding.btnAction7.setOnClickListener { openType("CAMPUS_SERVICES") }

        binding.btnAction8.text = "Helpdesk & Support"
        binding.btnAction8.setIconResource(R.drawable.ic_settings)
        binding.btnAction8.setOnClickListener { openType("HELPDESK") }
    }

    private fun setupLecturerDashboard() {
        showActionButtons(6)

        binding.btnAction1.text = "Class Student List"
        binding.btnAction1.setIconResource(R.drawable.ic_students)
        binding.btnAction1.setOnClickListener { openType("STUDENTS") }

        binding.btnAction2.text = "Record Marks & Grades"
        binding.btnAction2.setIconResource(R.drawable.ic_marks)
        binding.btnAction2.setOnClickListener { openType("ENTER_MARKS") }

        binding.btnAction3.text = "Course Assignments"
        binding.btnAction3.setIconResource(R.drawable.ic_notes)
        binding.btnAction3.setOnClickListener { openType("ASSIGNMENTS") }

        binding.btnAction4.text = "Teaching Schedule"
        binding.btnAction4.setIconResource(R.drawable.ic_results)
        binding.btnAction4.setOnClickListener { openType("TIMETABLE") }

        binding.btnAction5.text = "Course Catalog"
        binding.btnAction5.setIconResource(R.drawable.ic_courses)
        binding.btnAction5.setOnClickListener { openType("COURSES") }

        binding.btnAction6.text = "University News"
        binding.btnAction6.setIconResource(R.drawable.ic_news)
        binding.btnAction6.setOnClickListener { openType("NEWS") }
    }

    private fun setupStaffDashboard() {
        showActionButtons(2)

        binding.btnAction1.text = "Helpdesk Requests"
        binding.btnAction1.setIconResource(R.drawable.ic_settings)
        binding.btnAction1.setOnClickListener { openType("HELPDESK") }

        binding.btnAction2.text = "University News"
        binding.btnAction2.setIconResource(R.drawable.ic_news)
        binding.btnAction2.setOnClickListener { openType("NEWS") }
    }

    private fun setupAdminDashboard() {
        showActionButtons(6)

        binding.btnAction1.text = "Manage Users"
        binding.btnAction1.setIconResource(R.drawable.ic_students)
        binding.btnAction1.setOnClickListener { openType("USERS") }

        binding.btnAction2.text = "Manage Courses"
        binding.btnAction2.setIconResource(R.drawable.ic_courses)
        binding.btnAction2.setOnClickListener { openType("COURSES") }

        binding.btnAction3.text = "Academic Reports"
        binding.btnAction3.setIconResource(R.drawable.ic_reports)
        binding.btnAction3.setOnClickListener { openType("REPORTS") }

        binding.btnAction4.text = "Publish News"
        binding.btnAction4.setIconResource(R.drawable.ic_news)
        binding.btnAction4.setOnClickListener { openType("NEWS") }

        binding.btnAction5.text = "Timetables & Exams"
        binding.btnAction5.setIconResource(R.drawable.ic_results)
        binding.btnAction5.setOnClickListener { openType("TIMETABLE") }

        binding.btnAction6.text = "Helpdesk Support"
        binding.btnAction6.setIconResource(R.drawable.ic_settings)
        binding.btnAction6.setOnClickListener { openType("HELPDESK") }
    }
}