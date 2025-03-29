package com.gawebersama.gawekuy.data.repository

import android.util.Log
import com.gawebersama.gawekuy.data.dataclass.ServiceModel
import com.gawebersama.gawekuy.data.dataclass.ServiceSelectionModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class ServiceRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "ServiceRepository"
    }

    private fun ServiceModel.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "serviceId" to serviceId,
            "userId" to userId,
            "serviceName" to serviceName,
            "serviceDesc" to serviceDesc,
            "imageBannerUrl" to imageBannerUrl,
            "serviceTypes" to serviceTypes,
            "createdAt" to createdAt
        )
    }

    private fun Map<String, Any>.toService(): ServiceModel {
        return ServiceModel(
            serviceId = this["serviceId"] as? String ?: "",
            userId = this["userId"] as? String ?: "",
            serviceName = this["serviceName"] as? String ?: "",
            serviceDesc = this["serviceDesc"] as? String ?: "",
            imageBannerUrl = this["imageBannerUrl"] as? String ?: "",
            serviceTypes = (this["serviceTypes"] as? List<HashMap<String, Any>>)?.map {
                ServiceSelectionModel(
                    name = it["name"] as? String ?: "",
                    price = (it["price"] as? Number)?.toDouble() ?: 0.0
                )
            } ?: emptyList(),
            createdAt = this["createdAt"] as? Timestamp ?: Timestamp.now()
        )
    }

    suspend fun createService(serviceName: String, serviceDesc: String, imageBannerUrl: String, serviceTypes: List<ServiceSelectionModel>): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val serviceId = firestore.collection("services").document().id // Auto-generate ID

            val serviceModelData = ServiceModel(
                serviceId = serviceId,
                userId = userId,
                serviceName = serviceName,
                serviceDesc = serviceDesc,
                imageBannerUrl = imageBannerUrl,
                serviceTypes = serviceTypes,
                createdAt = Timestamp.now()
            ).toHashMap()

            firestore.collection("services").document(serviceId).set(serviceModelData).await()
            Log.d(TAG, "Service created with ID: $serviceId") // Debug
            Pair(true, "Jasa berhasil dibuat")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error creating service: ${e.message}") // Debug
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun getUserServices(): List<ServiceModel> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return emptyList()
            val snapshot = firestore.collection("services")
                .whereEqualTo("userId", userId) // Ambil jasa milik user
                .get()
                .await()

            val services = snapshot.documents.mapNotNull { doc ->
                val service = doc.data?.toService()
                Log.d(TAG, "Fetched Service ID: ${service?.serviceId}") // Debug setiap serviceId
                service
            }

            services
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user services: ${e.message}")
            emptyList()
        }
    }

    suspend fun getServiceById(serviceId: String): ServiceModel? {
        return try {
            val snapshot = firestore.collection("services").document(serviceId).get().await()
            snapshot.data?.toService()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching service by ID: ${e.message}")
            null
        }
    }

    suspend fun updateService(serviceId: String, serviceName: String, serviceDesc: String, imageBannerUrl: String, serviceTypes: List<ServiceSelectionModel>): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")

            val serviceRef = firestore.collection("services").document(serviceId)
            val serviceSnapshot = serviceRef.get().await()

            if (serviceSnapshot.exists() && serviceSnapshot.getString("userId") == userId) {
                serviceRef.update(
                    mapOf(
                        "serviceName" to serviceName,
                        "serviceDesc" to serviceDesc,
                        "imageBannerUrl" to imageBannerUrl,
                        "serviceTypes" to serviceTypes
                    )
                ).await()
                Pair(true, "Jasa berhasil diperbarui")
            } else {
                Pair(false, "Anda tidak berhak mengupdate jasa ini")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun updateServiceImageUrl(serviceId : String, imageBannerUrl: String): Pair<Boolean, String?> {
        return try {
            val serviceRef = firestore.collection("services").document(serviceId)

            serviceRef.update("imageBannerUrl", imageBannerUrl).await()

            Log.d(TAG, "Service banner URL updated successfully for service $serviceId")
            Pair(true, "URL banner jasa berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            val errorMessage = getErrorMessage(e)
            Log.e(TAG, "Error updating service banner URL: $errorMessage")

            Pair(false, errorMessage)
        }
    }

    suspend fun deleteService(serviceId: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val serviceRef = firestore.collection("services").document(serviceId)
            val serviceSnapshot = serviceRef.get().await()

            if (serviceSnapshot.exists() && serviceSnapshot.getString("userId") == userId) {
                serviceRef.delete().await()
            } else {
                return Pair(false, "Anda tidak berhak menghapus jasa ini")
            }

            Log.d(TAG, "Service deleted successfully")
            Pair(true, "Jasa berhasil dihapus")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error deleting service: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    // Fungsi untuk menangani error Firebase
    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.", ignoreCase = true) == true ->
                "Terjadi kesalahan jaringan, coba lagi nanti"
            else -> e.message ?: "Terjadi kesalahan, coba lagi nanti"
        }
    }
}