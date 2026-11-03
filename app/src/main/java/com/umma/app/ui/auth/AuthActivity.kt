package com.umma.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.umma.app.data.repository.AuthRepository
import com.umma.app.databinding.ActivityAuthBinding
import com.umma.app.ui.main.MainActivity
import com.umma.app.util.SessionManager
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var repository: AuthRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AuthRepository(this)
        session = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(email, password)
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val input = TextInputEditText(this)
        input.hint = "Registered Email Address"

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your email address to receive password reset instructions.")
            .setView(input)
            .setPositiveButton("Send Reset Link") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) {
                    Toast.makeText(this, "Password reset instructions sent to $email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.login(email, password)

            binding.btnLogin.isEnabled = true
            binding.progressBar.visibility = View.GONE

            result.onSuccess { user ->
                session.save(user)
                Toast.makeText(this@AuthActivity, "Welcome, ${user.fullName}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                finish()
            }.onFailure { error ->
                Toast.makeText(this@AuthActivity, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}