package com.gawebersama.gawekuy.data.repository

import com.gawebersama.gawekuy.data.dataclass.UserModel
import com.gawebersama.gawekuy.data.enum.UserRole
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.collections.hashMapOf
import kotlin.coroutines.cancellation.CancellationException

class UserRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "UserRepository"
    }

    private fun UserModel.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "userId" to userId,
            "email" to email,
            "name" to name,
            "phone" to phone,
            "role" to role,
            "profileImageUrl" to profileImageUrl,
            "biography" to biography,
            "createdAt" to createdAt,
            "userStatus" to userStatus,
            "accountStatus" to accountStatus
        )
    }

    private fun Map<String, Any>.toUser(): UserModel {
        return UserModel(
            userId = this["userId"] as? String,
            email = this["email"] as? String,
            name = this["name"] as? String,
            phone = this["phone"] as? String,
            role = this["role"] as? String,
            profileImageUrl = this["profileImageUrl"] as? String,
            biography = this["biography"] as? String,
            createdAt = this["createdAt"] as? Timestamp ?: Timestamp.now(),
            userStatus = this["userStatus"] as? String,
            accountStatus = this["accountStatus"] as? Boolean
        )
    }

    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

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

            authResult.user?.sendEmailVerification()

            val userModelData = UserModel(
                userId = userId,
                email = email,
                name = name,
                phone = phone,
                role = role,
                profileImageUrl = null,
                biography = null,
                createdAt = Timestamp.now(),
                userStatus = null,
                accountStatus = true
            ).toHashMap()

            firestore.collection("users").document(userId).set(userModelData).await()

            println("UserRepository: register success & data saved to Firestore")
            Pair(true, "Silakan verifikasi email Anda sebelum login")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("UserRepository: register failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun login(email: String, password: String): Pair<Boolean, String?> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            println("UserRepository: login success")
            Pair(true, null)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("UserRepository: login failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun getUserData(): UserModel? {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return null
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                document.data?.toUser()
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("UserRepository: failed to get user data - ${e.message}")
            null
        }
    }

    suspend fun updateProfile(name: String, phone: String, userStatus: String, biography: String, profileImageUrl: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = firestore.collection("users").document(userId)

            userRef.update(
                mapOf(
                    "name" to name,
                    "phone" to phone,
                    "biography" to biography,
                    "userStatus" to userStatus,
                    "profileImageUrl" to profileImageUrl
                )
            ).await()

            println("UserRepository: profile updated")
            Pair(true, "Profil berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("UserRepository: update profile failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun forgotPassword(email: String): Pair<Boolean, String?> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            println("UserRepository: forgot password success")
            Pair(true, "Silakan cek email Anda untuk reset password")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("UserRepository: forgot password failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        firestore.clearPersistence()
        if (firebaseAuth.currentUser == null) {
            println("UserRepository: logout success")
        } else {
            println("UserRepository: logout failed")
        }
    }

    suspend fun becomeFreelancer(): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = firestore.collection("users").document(userId)

            userRef.update("role", UserRole.FREELANCER.toString()).await()

            println("UserRepository: become freelancer success")
            Pair(true, "Berhasil menjadi freelancer")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("UserRepository: become freelancer failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun updateAccountStatus(isActive: Boolean): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = firestore.collection("users").document(userId)

            userRef.update("accountStatus", isActive).await()

            println("UserRepository: account status updated")
            Pair(true, "Status akun berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            println("UserRepository: update account status failed - ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun updateProfileImageUrl(imageUrl: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")

            val userRef = firestore.collection("users").document(userId)

            userRef.update("profileImageUrl", imageUrl).await()

            println("UserRepository: Profile image URL updated successfully for user $userId")
            Pair(true, "URL foto profil berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            val errorMessage = getErrorMessage(e)
            println("UserRepository: Update profile image URL failed - ${e.localizedMessage}")

            Pair(false, errorMessage)
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
            e.message?.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.", ignoreCase = true) == true ->
                "Terjadi kesalahan jaringan, coba lagi nanti"
            else -> e.message ?: "Terjadi kesalahan, coba lagi nanti"
        }
    }
}
