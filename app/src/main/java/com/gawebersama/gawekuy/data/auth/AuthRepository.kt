package com.gawebersama.gawekuy.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun isLoggedIn(): Boolean {
        if (firebaseAuth.currentUser != null) {
            println("AuthRepository: login success")
            return true
        } else {
            println("AuthRepository: login failed")
            return false
        }
    }

    suspend fun register(email: String, password: String): Boolean {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            println("AuthRepository: register success")
            login(email, password)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("AuthRepository: register failed - ${e.message}")
            false
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            println("AuthRepository: login success")
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("AuthRepository: login failed - ${e.message}")
            false
        }
    }

    fun getProfile(): String {
        return firebaseAuth.currentUser?.email ?: "Guest"
    }

    fun logout() {
        firebaseAuth.signOut()
        println("AuthRepository: logout success")
    }
}
