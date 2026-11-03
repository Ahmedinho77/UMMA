package com.umma.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.umma.app.databinding.ActivityProfileBinding
import com.umma.app.ui.auth.AuthActivity
import com.umma.app.util.SessionManager
import coil.load

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var session: SessionManager

    // This launcher opens the phone's gallery to pick a photo
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Save the URI to session and display it
            session.updateProfilePhoto(it.toString())
            binding.ivProfilePhoto.load(it) {
                placeholder(android.R.drawable.ic_menu_gallery)
                crossfade(true)
            }
            Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        val user = session.getUser() ?: return

        // Populate UI
        binding.tvName.text = user.fullName
        binding.tvRegNo.text = user.registrationNo
        binding.tvEmail.text = "Email: ${user.email}"
        binding.tvDept.text = "Department: ${user.department}"
        binding.tvRole.text = "Role: ${user.role.name}"

        // Load photo if it exists
        user.photoUri?.let {
            binding.ivProfilePhoto.load(it)
        }

        // Tap to change photo
        binding.cardPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Logout logic
        binding.btnLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(this, AuthActivity::class.java))
            finishAffinity() // Clears the backstack
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(android.content.Intent(this, EditProfileActivity::class.java))
        }
    }
    override fun onResume() {
        super.onResume()
        // Refresh user data when coming back from EditProfile
        val user = session.getUser() ?: return
        binding.tvName.text = user.fullName
    }
}