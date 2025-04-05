package com.gawebersama.gawekuy.data.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gawebersama.gawekuy.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _authStatus = MutableLiveData<Pair<Boolean, String?>>()
    val authStatus: LiveData<Pair<Boolean, String?>> get() = _authStatus

    private val _authRegister = MutableLiveData<Pair<Boolean, String?>>()
    val authRegister: LiveData<Pair<Boolean, String?>> get() = _authRegister

    private val _authLogin = MutableLiveData<Pair<Boolean, String?>>()
    val authLogin: LiveData<Pair<Boolean, String?>> get() = _authLogin

    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> get() = _userId

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> get() = _userRole

    private val _userPhone = MutableLiveData<String?>()
    val userPhone: LiveData<String?> get() = _userPhone

    private val _userStatus = MutableLiveData<String?>()
    val userStatus: LiveData<String?> get() = _userStatus

    private val _userImageUrl = MutableLiveData<String?>()
    val userImageUrl: LiveData<String?> get() = _userImageUrl

    private val _userBiography = MutableLiveData<String?>()
    val userBiography: LiveData<String?> get() = _userBiography

    private val _accountStatus = MutableLiveData<Boolean?>()
    val accountStatus: LiveData<Boolean?> get() = _accountStatus

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        getUser()
    }

    fun registerAccountOnly(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.registerAccountOnly(email, password)
            _authRegister.postValue(result)
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val result = userRepository.resendVerificationEmail()
            _authStatus.postValue(result)
        }
    }

    fun completeUserRegistration(context: Context) {
        viewModelScope.launch {
            val result = userRepository.completeUserRegistration(context)
            _authStatus.postValue(result)
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.login(email, password)
            _authLogin.postValue(result)
        }
    }

    fun getUser() {
        viewModelScope.launch {
            val userData = userRepository.getUserData()

            if (userData != null) {
                _userId.postValue(userData.userId)
                _userName.postValue(userData.name)
                _userRole.postValue(userData.role)
                _userPhone.postValue(userData.phone)
                _userStatus.postValue(userData.userStatus ?: "")
                _userBiography.postValue(userData.biography ?: "")
                _userImageUrl.postValue(userData.profileImageUrl)
                _accountStatus.postValue(userData.accountStatus)
            }
        }
    }

    fun updateProfile(name: String, phone: String, userStatus: String, biography: String, profileImageUrl: String) {
        viewModelScope.launch {
            val result = userRepository.updateProfile(name, phone, userStatus, biography, profileImageUrl)
            _authStatus.postValue(result)

            if (result.first) {
                _userName.postValue(name)
                _userPhone.postValue(phone)
                _userStatus.postValue(userStatus)
                _userBiography.postValue(biography)
                _userImageUrl.postValue(profileImageUrl)
                _errorMessage.postValue(null)
            }
        }
    }

    fun updateProfileImageUrl(imageUrl: String) {
        viewModelScope.launch {
            val result = userRepository.updateProfileImageUrl(imageUrl)
            if (result.first) {
                _userImageUrl.postValue(imageUrl)
            }
        }
    }

    fun becomeFreelancer() {
        viewModelScope.launch {
            val result = userRepository.becomeFreelancer()
            _authStatus.postValue(result)
            if (result.first) {
                _userRole.postValue("FREELANCER")
            }
        }
    }

    fun updateAccountStatus(isActive: Boolean) {
        viewModelScope.launch {
            val (success, message) = userRepository.updateAccountStatus(isActive)

            if (success) {
                _accountStatus.postValue(isActive)
            } else {
                _errorMessage.postValue(message)
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            userRepository.logout()
            _userId.postValue(null)
            _userName.postValue(null)
            _userRole.postValue(null)
            _userPhone.postValue(null)
            _userStatus.postValue(null)
            _userImageUrl.postValue(null)
            _userBiography.postValue(null)
            _accountStatus.postValue(null)
            _errorMessage.postValue(null)
            _authStatus.postValue(Pair(false, "User logged out"))
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            userRepository.forgotPassword(email)
            _authStatus.postValue(Pair(true, "Silakan cek email Anda untuk reset password"))
        }
    }
}
