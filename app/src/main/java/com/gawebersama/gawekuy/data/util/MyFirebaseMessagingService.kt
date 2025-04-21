package com.gawebersama.gawekuy.data.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = "MyFirebaseMessagingService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore

        val tokenData = mapOf(
            "fcmToken" to token,
            "lastUpdated" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .update(tokenData)
            .addOnSuccessListener {
                Log.d("FCM", "Token berhasil disimpan")
            }
            .addOnFailureListener {
                Log.e("FCM", "Gagal menyimpan token: ${it.message}")
            }
    }
}