package com.gawebersama.gawekuy.data.repository

import android.content.Context
import android.util.Log
import com.gawebersama.gawekuy.data.datamodel.PaymentInfoModel
import com.gawebersama.gawekuy.data.datamodel.UserModel
import com.gawebersama.gawekuy.data.datamodel.UserWithPaymentInfoModel
import com.gawebersama.gawekuy.data.datastore.UserAccountPreferences
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

    private val userCollection = firestore.collection("users")

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
            "accountStatus" to accountStatus,
            "paymentInfo" to paymentInfo
        )
    }

    private fun Map<String, Any>.toUser(): UserModel {
        return UserModel(
            userId = this["userId"] as String,
            email = this["email"] as String,
            name = this["name"] as String,
            phone = this["phone"] as String,
            role = this["role"] as String,
            profileImageUrl = this["profileImageUrl"] as? String,
            biography = this["biography"] as? String,
            createdAt = this["createdAt"] as Timestamp,
            userStatus = this["userStatus"] as? String,
            accountStatus = this["accountStatus"] as Boolean,
            paymentInfo = this["paymentInfo"] as? PaymentInfoModel
        )
    }

    suspend fun registerAccountOnly(email: String, password: String): Pair<Boolean, String?> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.sendEmailVerification()
            Pair(true, "Silakan verifikasi email Anda sebelum login.")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun resendVerificationEmail(): Pair<Boolean, String?> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.reload()

            if (user != null && user.isEmailVerified) {
                return Pair(false, "Email sudah diverifikasi")
            }

            user?.sendEmailVerification()?.await()
            Log.d("Auth", "Verification email sent to ${user?.email}")

            Pair(true, "Silakan cek email Anda untuk verifikasi akun Anda.")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun completeUserRegistration(context: Context): Pair<Boolean, String?> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.reload()
            user?.getIdToken(true)?.await()

            val refreshedUser = FirebaseAuth.getInstance().currentUser

            if (refreshedUser != null && refreshedUser.isEmailVerified) {
                val userDocRef = userCollection.document(refreshedUser.uid)
                val existingUser = userDocRef.get().await()

                if (existingUser.exists()) {
                    return Pair(true, null)
                }

                val prefs = UserAccountPreferences(context)
                val tempUser = prefs.getTempUser() ?: return Pair(false, "Data user tidak ditemukan")

                val userModelData = UserModel(
                    userId = refreshedUser.uid,
                    email = refreshedUser.email ?: "",
                    name = tempUser.name,
                    phone = tempUser.phone,
                    role = tempUser.role,
                    profileImageUrl = null,
                    biography = null,
                    createdAt = Timestamp.now(),
                    userStatus = null,
                    accountStatus = true
                ).toHashMap()

                userDocRef.set(userModelData).await()
                prefs.clearTempUser()

                return Pair(true, "Akun berhasil dibuat sepenuhnya!")
            } else {
                return Pair(false, "Akun belum terverifikasi")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun login(email: String, password: String): Pair<Boolean, String?> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "User logged in with email: $email")
            Pair(true, null)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error logging in user: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun getUserData(): UserWithPaymentInfoModel? {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return null
            val userSnapshot = userCollection.document(userId).get().await()
            val userData = userSnapshot.toObject(UserModel::class.java)

            if (userData == null) {
                Log.e(TAG, "User data not found for userId: $userId")
                return null
            }

            val paymentInfo = userData.paymentInfo
            UserWithPaymentInfoModel(userData, paymentInfo)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user data: ${e.message}")
            null
        }
    }

    suspend fun updateProfile(name: String, phone: String, userStatus: String, biography: String, profileImageUrl: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = userCollection.document(userId)

            userRef.update(
                mapOf(
                    "name" to name,
                    "phone" to phone,
                    "biography" to biography,
                    "userStatus" to userStatus,
                    "profileImageUrl" to profileImageUrl
                )
            ).await()

            Log.d(TAG, "User profile updated with ID: $userId")
            Pair(true, "Profil berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error updating user profile: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun forgotPassword(email: String): Pair<Boolean, String?> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to $email")
            Pair(true, "Silakan cek email Anda untuk reset password")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error sending password reset email: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        firestore.clearPersistence()
        if (firebaseAuth.currentUser == null) {
            Log.d(TAG, "User logged out")
        } else {
            Log.e(TAG, "Error logging out user")
        }
    }

    suspend fun becomeFreelancer(): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = userCollection.document(userId)

            userRef.update("role", UserRole.FREELANCER.toString()).await()

            Log.d(TAG, "User became freelancer with ID: $userId")
            Pair(true, "Berhasil menjadi freelancer")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error becoming freelancer: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun updateAccountStatus(isActive: Boolean): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = userCollection.document(userId)

            userRef.update("accountStatus", isActive).await()

            Log.d(TAG, "User account status updated with ID: $userId")
            Pair(true, "Status akun berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error updating account status: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun updateProfileImageUrl(imageUrl: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = userCollection.document(userId)
            userRef.update("profileImageUrl", imageUrl).await()

            Log.d(TAG, "User profile image URL updated with ID: $userId")
            Pair(true, "URL foto profil berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            val errorMessage = getErrorMessage(e)
            Log.e(TAG, "Error updating profile image URL: $errorMessage")

            Pair(false, errorMessage)
        }
    }

    suspend fun updatePaymentInfo(paymentInfo: PaymentInfoModel?): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val userRef = userCollection.document(userId)
            userRef.update("paymentInfo", paymentInfo).await()

            Log.d(TAG, "User payment info updated with ID: $userId")
            Pair(true, "Pembayaran berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
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
            e.message?.contains("PERMISSION_DENIED: Missing or insufficient permissions.", ignoreCase = true) == true ->
                "Anda tidak memiliki izin untuk melakukan ini"
            e.message?.contains("We have blocked all requests from this device due to unusual activity. Try again later.", ignoreCase = true) == true ->
                "Terlalu banyak permintaan, coba lagi nanti"
            else -> e.message ?: "Terjadi kesalahan, coba lagi nanti"
        }
    }
}
