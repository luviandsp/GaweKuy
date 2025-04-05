package com.gawebersama.gawekuy.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.gawebersama.gawekuy.data.datastore.LoginPreferences
import com.gawebersama.gawekuy.data.datastore.AppPreferences
import com.gawebersama.gawekuy.databinding.ActivityAuthBinding
import com.gawebersama.gawekuy.ui.main.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var appPreferences: AppPreferences
    private lateinit var loginPreferences: LoginPreferences
    private var loginStatus : Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen ditahan dulu sampai login status dicek
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        appPreferences = AppPreferences(this)
        loginPreferences = LoginPreferences(this)
        applyTheme()

        // Tahan splash sampai loginStatus selesai dicek
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }

        lifecycleScope.launch {
            loginStatus = loginPreferences.getLoginStatus()
            if (loginStatus == true) {
                // Sudah login → langsung ke MainActivity
                Intent(this@AuthActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(this)
                }
            } else {
                // Belum login → lanjut tampilkan Auth UI
                keepSplash = false
                setupContentView()
            }
        }
    }

    private fun setupContentView() {
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun applyTheme() {
        val isDarkMode = runBlocking { appPreferences.darkModeFlow.first() }
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}