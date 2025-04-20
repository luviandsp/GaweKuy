package com.gawebersama.gawekuy.ui.profile

import android.app.AlertDialog
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
import com.gawebersama.gawekuy.data.datastore.AppPreferences
import com.gawebersama.gawekuy.data.datastore.LoginPreferences
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.ActivitySettingBinding
import com.gawebersama.gawekuy.databinding.DialogDeleteAccountBinding
import com.gawebersama.gawekuy.databinding.DialogSwitchAccountStatusBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var appPreferences: AppPreferences
    private lateinit var loginPreferences: LoginPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appPreferences = AppPreferences(this)
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

            btnDeleteAccount.setOnClickListener {
                showDeleteAccountDialog()
            }
        }
    }

    private fun showDeleteAccountDialog() {
        val dialogBinding = DialogDeleteAccountBinding.inflate(layoutInflater)
        val deleteDialog = AlertDialog.Builder(this@SettingActivity).setView(dialogBinding.root).create()

        with(dialogBinding) {
            btnCancel.setOnClickListener {
                deleteDialog.dismiss()
            }

            btnDelete.setOnClickListener {
                lifecycleScope.launch {
                    loginPreferences.setLoginStatus(false)
                }

                userViewModel.deleteAccount()
                deleteDialog.dismiss()
            }

            deleteDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            deleteDialog.show()
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
            appPreferences.saveTheme(isDarkMode)

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
            appPreferences.saveNotification(isEnabled)
        }
    }

    private fun saveAccountStatus(isActive: Boolean) {
        if (isActive == false) {
            showSwitchOffDialog()
        } else {
            userViewModel.updateAccountStatus(true)
        }
    }

    private fun showSwitchOffDialog() {
        val dialogBinding = DialogSwitchAccountStatusBinding.inflate(layoutInflater)
        val switchDialog = AlertDialog.Builder(this@SettingActivity).setView(dialogBinding.root).create()

        with(dialogBinding) {
            btnCancel.setOnClickListener {
                binding.switchAccountStatus.isChecked = true
                switchDialog.dismiss()
            }

            btnOff.setOnClickListener {
                userViewModel.updateAccountStatus(false)
                switchDialog.dismiss()
            }

            switchDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            switchDialog.show()
        }
    }

    private fun applySavedTheme() {
        lifecycleScope.launch {
            val isDarkMode = appPreferences.darkModeFlow.first()

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
                        appPreferences.darkModeFlow.collectLatest { isDarkMode ->
                            rbLight.isChecked = !isDarkMode
                            rbDark.isChecked = isDarkMode
                        }
                    }

                    launch {
                        appPreferences.notificationsFlow.collectLatest { isEnabled ->
                            switchNotification.isChecked = isEnabled
                            switchNotification.text = getString(if (isEnabled) R.string.on else R.string.off)
                        }
                    }
                }
            }
        }
    }
}

