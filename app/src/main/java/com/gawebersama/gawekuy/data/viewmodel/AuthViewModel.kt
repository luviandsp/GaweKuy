package com.gawebersama.gawekuy.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gawebersama.gawekuy.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _authStatus = MutableLiveData<Pair<Boolean, String?>>()
    val authStatus: LiveData<Pair<Boolean, String?>> get() = _authStatus

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> get() = _userRole

    private val _userPhone = MutableLiveData<String?>()
    val userPhone: LiveData<String?> get() = _userPhone

    private val _userImageUrl = MutableLiveData<String?>()
    val userImageUrl: LiveData<String?> get() = _userImageUrl

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    init {
        // Cek status login saat ViewModel dibuat
        _isLoggedIn.value = authRepository.isLoggedIn()
        if (_isLoggedIn.value == true) {
            getUser()
        }
    }

    // Fungsi untuk register
    fun registerUser(email: String, password: String, name: String, phone: String, role: String) {
        viewModelScope.launch {
            val result = authRepository.register(email, password, name, phone, role)
            _authStatus.postValue(result)

            // Jika registrasi berhasil, update status login
            if (result.first) {
                _isLoggedIn.postValue(true)
                getUser() // Ambil data user setelah register
            }
        }
    }

    // Fungsi untuk login
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _authStatus.postValue(result)

            // Jika login berhasil, update status login
            if (result.first) {
                _isLoggedIn.postValue(true)
                getUser() // Ambil data user setelah login
            }
        }
    }

    // Ambil nama pengguna dari Firestore
    fun getUser() {
        viewModelScope.launch {
            val userData = authRepository.getUserData()

            if (userData != null) {
                _userName.postValue(userData.name ?: "")
                _userRole.postValue(userData.role ?: "Client")
                _userPhone.postValue(userData.phone ?: "")
                _userImageUrl.postValue(userData.profileImageUrl)
            }
        }
    }

    fun updateProfile(name: String, profileImageUrl: String) {
        viewModelScope.launch {
            val result = authRepository.updateProfile(name, profileImageUrl)
            _authStatus.postValue(result)
        }
    }

    // Logout pengguna
    fun logoutUser() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.postValue(false)
            _userName.postValue(null)
            _userRole.postValue(null)
            _userPhone.postValue(null)
            _authStatus.postValue(Pair(false, "User logged out"))
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            authRepository.forgotPassword(email)
        }
    }
}
