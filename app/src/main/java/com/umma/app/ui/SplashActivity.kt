package com.umma.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.umma.app.databinding.ActivitySplashBinding
import com.umma.app.ui.auth.AuthActivity
import com.umma.app.ui.main.MainActivity
import com.umma.app.util.SessionManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivitySplashBinding.inflate(layoutInflater).also { setContentView(it.root) }

        Handler(Looper.getMainLooper()).postDelayed({
            val next = if (SessionManager(this).isLoggedIn())
                MainActivity::class.java else AuthActivity::class.java
            startActivity(Intent(this, next))
            finish()
        }, 1500)
    }
}