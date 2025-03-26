package com.gawebersama.gawekuy.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Cek apakah user sudah login
    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Mendapatkan data user yang sedang login
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    // Registrasi user baru
    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: String
    ): Pair<Boolean, String?> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Pair(false, "Gagal mendapatkan ID pengguna")

            // Kirim email verifikasi
            authResult.user?.sendEmailVerification()

            // Data user yang akan disimpan di Firestore
            val userData = mapOf(
                "userId" to userId,
                "email" to email,
                "name" to name,
                "phone" to phone,
                "role" to role, // Bisa "client" atau "freelancer"
                "createdAt" to System.currentTimeMillis()
            )

            // Simpan ke Firestore
            firestore.collection("users").document(userId).set(userData).await()

            println("AuthRepository: register success & data saved to Firestore")
            Pair(true, "Silakan verifikasi email Anda sebelum login")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("AuthRepository: register failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    // Login user
    suspend fun login(email: String, password: String): Pair<Boolean, String?> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            println("AuthRepository: login success")
            Pair(true, null)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("AuthRepository: login failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    // Mengambil data user dari Firestore
    suspend fun getUserData(): Map<String, Any>? {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return null
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                document.data
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("AuthRepository: failed to get user data - ${e.message}")
            null
        }
    }

    suspend fun getUserName(): String? {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return null
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                println("AuthRepository: getUserName success")
                document.getString("name")
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("AuthRepository: failed to get user data - ${e.message}")
            null
        }
    }

    suspend fun getUserRole(): String? {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return null
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                println("AuthRepository: getUserRole success")
                document.getString("role")
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("AuthRepository: failed to get user data - ${e.message}")
            null
        }
    }

    // Update data profil user
    suspend fun updateProfile(name: String, phone: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")

            val updates = mapOf(
                "name" to name,
                "phone" to phone
            )

            firestore.collection("users").document(userId).update(updates).await()

            println("AuthRepository: profile updated")
            Pair(true, "Profil berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("AuthRepository: update profile failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    // Lupa password - Kirim email reset password
    suspend fun forgotPassword(email: String): Pair<Boolean, String?> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            println("AuthRepository: forgot password success")
            Pair(true, "Silakan cek email Anda untuk reset password")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("AuthRepository: forgot password failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    // Logout user dan hapus cache Firestore
    fun logout() {
        firebaseAuth.signOut()
        firestore.clearPersistence() // Hapus cache Firestore
        if (firebaseAuth.currentUser == null) {
            println("AuthRepository: logout success")
        } else {
            println("AuthRepository: logout failed")
        }
    }

    // Fungsi untuk menangani error Firebase
    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("email address is badly formatted", ignoreCase = true) == true ->
                "Format email tidak valid"
            e.message?.contains("password is invalid or the user does not have a password", ignoreCase = true) == true ->
                "Password salah atau akun tidak memiliki password"
            e.message?.contains("There is no user record corresponding to this identifier", ignoreCase = true) == true ->
                "Email belum terdaftar"
            e.message?.contains("The email address is already in use", ignoreCase = true) == true ->
                "Email sudah digunakan"
            e.message?.contains("Password should be at least 8 characters", ignoreCase = true) == true ->
                "Password minimal harus 8 karakter"
            e.message?.contains("The supplied auth credential is incorrect, malformed or has expired.", ignoreCase = true) == true ->
                "Email atau password salah"
            else -> e.message ?: "Terjadi kesalahan, coba lagi nanti"
        }
    }
}
