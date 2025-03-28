package com.gawebersama.gawekuy.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.datastore.UserPreferences
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.ActivitySettingBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.getValue

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(this)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        applySavedTheme()
        observeViewModel()
        loadPreferences()
        initViews()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener { finish() }

            rbLight.setOnClickListener { saveTheme(false) }
            rbDark.setOnClickListener { saveTheme(true) }

            switchNotification.setOnCheckedChangeListener { _, isChecked ->
                saveNotification(isChecked)
            }

            switchAccountStatus.setOnCheckedChangeListener { _, isChecked ->
                saveAccountStatus(isChecked)
            }
        }
    }

    private fun observeViewModel() {
        userViewModel.accountStatus.observe(this@SettingActivity) { isActive ->
            with (binding) {
                switchAccountStatus.isChecked = isActive == true
                switchAccountStatus.text = getString(if (isActive == true) R.string.on else R.string.off)
            }
        }

        userViewModel.errorMessage.observe(this@SettingActivity) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTheme(isDarkMode: Boolean) {
        lifecycleScope.launch {
            userPreferences.saveTheme(isDarkMode)

            val currentMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO

            if (AppCompatDelegate.getDefaultNightMode() != currentMode) {
                AppCompatDelegate.setDefaultNightMode(currentMode)
                recreate()
            }
        }
    }

    private fun saveNotification(isEnabled: Boolean) {
        lifecycleScope.launch {
            userPreferences.saveNotification(isEnabled)
        }
    }

    private fun saveAccountStatus(isActive: Boolean) {
        userViewModel.updateAccountStatus(isActive)
    }

    private fun applySavedTheme() {
        lifecycleScope.launch {
            val isDarkMode = userPreferences.darkModeFlow.first()

            val currentMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO

            if (AppCompatDelegate.getDefaultNightMode() != currentMode) {
                AppCompatDelegate.setDefaultNightMode(currentMode)
                recreate()
            }
        }
    }

    private fun loadPreferences() {
        with (binding) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        userPreferences.darkModeFlow.collectLatest { isDarkMode ->
                            rbLight.isChecked = !isDarkMode
                            rbDark.isChecked = isDarkMode
                        }
                    }

                    launch {
                        userPreferences.notificationsFlow.collectLatest { isEnabled ->
                            switchNotification.isChecked = isEnabled
                            switchNotification.text = getString(if (isEnabled) R.string.on else R.string.off)
                        }
                    }
                }
            }
        }
    }
}

