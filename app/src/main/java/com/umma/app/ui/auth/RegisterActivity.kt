package com.umma.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.umma.app.R
import com.umma.app.data.model.UserEntity
import com.umma.app.data.repository.AuthRepository
import com.umma.app.databinding.ActivityRegisterBinding
import com.umma.app.ui.main.MainActivity
import com.umma.app.util.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repository: AuthRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AuthRepository(this)
        session = SessionManager(this)

        val roles = arrayOf("STUDENT", "LECTURER", "STAFF", "ADMIN")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.etRole.setAdapter(adapter)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val regNo = binding.etReg.text.toString().trim()
            val dept = binding.etDepartment.text.toString().trim()
            val role = binding.etRole.text.toString().trim().uppercase()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || regNo.isEmpty() || role.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performRegister(name, email, password, regNo, dept.ifEmpty { "General" }, role)
        }

        binding.tvLoginLink.setOnClickListener { finish() }
    }

    private fun performRegister(
        name: String,
        email: String,
        password: String,
        regNo: String,
        dept: String,
        role: String
    ) {
        binding.btnRegister.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val newUser = UserEntity(
                fullName = name,
                email = email,
                passwordHash = "",
                role = role,
                registrationNo = regNo,
                department = dept
            )

            val result = repository.register(newUser, password)

            binding.btnRegister.isEnabled = true
            binding.progressBar.visibility = View.GONE

            result.onSuccess { user ->
                session.save(user)
                Toast.makeText(this@RegisterActivity, "Welcome to UMMA, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finishAffinity()
            }.onFailure { error ->
                Toast.makeText(this@RegisterActivity, "Registration failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}