package com.gawebersama.gawekuy.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gawebersama.gawekuy.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.seconds


class StorageRepository {
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Storage) {
            transferTimeout = 120.seconds
        }
    }

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadImageToSupabase(imageFile: File, folderName : String, fileName: String): String? {
        return try {
            val storage = supabase.storage.from("gawekuy")

            val filePath = "$folderName/$fileName"

            // Upload file
            val response = withContext(Dispatchers.IO) {
                storage.upload(filePath, imageFile.readBytes()) {
                    upsert = true
                }
            }

            // URL Public untuk mengakses gambar
            val imageUrl = storage.publicUrl(filePath)
            println("Image uploaded: $imageUrl")
            imageUrl
        } catch (e: Exception) {
            println("Upload failed: ${e.message}")
            null
        }
    }

    suspend fun deleteImage(fileName: String): Boolean {
        return try {
            val storage = supabase.storage.from("gawekuy")
            storage.delete(fileName)
            println("Image deleted successfully")
            true
        } catch (e: Exception) {
            println("Delete failed: ${e.message}")
            false
        }
    }

    suspend fun saveImageUrlToFirestore(imageUrl: String): Boolean {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false

            val userRef = firestore.collection("users").document(userId)
            userRef.update("profileImageUrl", imageUrl).await()

            println("Image URL saved successfully")
            true
        } catch (e: Exception) {
            println("Failed to save image URL: ${e.message}")
            false
        }
    }
}