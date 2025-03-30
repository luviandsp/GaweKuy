package com.gawebersama.gawekuy.data.repository

import android.util.Log
import com.gawebersama.gawekuy.data.datamodel.ServiceModel
import com.gawebersama.gawekuy.data.datamodel.ServiceSelectionModel
import com.gawebersama.gawekuy.data.datamodel.ServiceWithUserModel
import com.gawebersama.gawekuy.data.datamodel.UserModel
import com.gawebersama.gawekuy.data.enum.FilterAndOrderService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction.ASCENDING
import com.google.firebase.firestore.Query.Direction.DESCENDING
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
            "serviceRating" to serviceRating,
            "serviceTypes" to serviceTypes,
            "minPrice" to minPrice,
            "serviceCategory" to serviceCategory,
            "serviceTags" to serviceTags,
            "createdAt" to createdAt
        )
    }

    private fun Map<String, Any>.toService(): ServiceModel {
        return ServiceModel(
            serviceId = this["serviceId"] as String,
            userId = this["userId"] as String,
            serviceName = this["serviceName"] as String,
            serviceDesc = this["serviceDesc"] as? String,
            imageBannerUrl = this["imageBannerUrl"] as? String,
            serviceRating = (this["serviceRating"] as Number).toDouble(),
            serviceTypes = (this["serviceTypes"] as List<*>?)?.mapNotNull { it as? Map<*, *> }?.map {
                ServiceSelectionModel(
                    name = it["name"] as String,
                    price = (it["price"] as Number).toDouble()
                )
            } ?: emptyList(),
            minPrice = (this["minPrice"] as Number).toDouble(),
            serviceCategory = this["serviceCategory"] as String,
            serviceTags = (this["serviceTags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            createdAt = this["createdAt"] as Timestamp
        )
    }

    suspend fun createService(serviceName: String, serviceDesc: String, imageBannerUrl: String, serviceCategory: String, serviceTypes: List<ServiceSelectionModel>, serviceTags: List<String>): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val serviceId = firestore.collection("services").document().id // Auto-generate ID

            val minPrice = serviceTypes.minOfOrNull { it.price.toDouble() } ?: 0.0

            val serviceModelData = ServiceModel(
                serviceId = serviceId,
                userId = userId,
                serviceName = serviceName,
                serviceDesc = serviceDesc,
                imageBannerUrl = imageBannerUrl,
                serviceRating = 0.0,
                serviceTypes = serviceTypes,
                minPrice = minPrice,
                serviceCategory = serviceCategory,
                serviceTags = serviceTags,
                createdAt = Timestamp.now()
            ).toHashMap()

            firestore.collection("services").document(serviceId).set(serviceModelData).await()
            Log.d(TAG, "Service created with ID: $serviceId")
            Pair(true, "Jasa berhasil dibuat")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error creating service: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun getUserServices(): List<ServiceWithUserModel> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return emptyList()

            // Ambil semua layanan milik user yang sedang login
            val serviceSnapshot = firestore.collection("services")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val services = serviceSnapshot.documents.mapNotNull { it.data?.toService() }

            // Ambil data user yang sedang login
            val userSnapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val userData = userSnapshot.toObject(UserModel::class.java)

            if (userData == null) {
                Log.e(TAG, "User data not found for userId: $userId")
                return emptyList()
            }

            // Gabungkan layanan dengan data pengguna
            services.map { service -> ServiceWithUserModel(service, userData) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user services: ${e.message}")
            emptyList()
        }
    }


    suspend fun getServiceById(serviceId: String): ServiceWithUserModel? {
        return try {
            val serviceSnapshot = firestore.collection("services").document(serviceId).get().await()
            val service = serviceSnapshot.toObject(ServiceModel::class.java) ?: return null

            val userSnapshot = firestore.collection("users").document(service.userId).get().await()
            val user = userSnapshot.toObject(UserModel::class.java) ?: return null

            ServiceWithUserModel(service, user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching service by ID: ${e.message}")
            null
        }
    }

    suspend fun getAllService(filter: FilterAndOrderService? = null): List<ServiceWithUserModel> {
        return try {
            val query = when (filter) {
                FilterAndOrderService.CHEAP -> firestore.collection("services").orderBy("minPrice", ASCENDING)
                FilterAndOrderService.EXPENSIVE -> firestore.collection("services").orderBy("minPrice", DESCENDING)
                FilterAndOrderService.RATING -> firestore.collection("services").orderBy("serviceRating", DESCENDING)
                null -> firestore.collection("services")
            }

            val serviceSnapshot = query.get().await()
            val services = serviceSnapshot.documents.mapNotNull { it.data?.toService() }

            // Ambil semua userId unik dari layanan
            val userIds = services.map { it.userId }.toSet()

            if (userIds.isEmpty()) return emptyList()

            // Ambil semua data pengguna dalam satu permintaan
            val userSnapshots = firestore.collection("users")
                .whereIn("userId", userIds.toList())
                .get()
                .await()

            val userMap = userSnapshots.documents.associate { it.id to it.toObject(UserModel::class.java) }

            // Gabungkan layanan dengan data pengguna
            services.mapNotNull { service ->
                val user = userMap[service.userId]
                if (user != null) {
                    ServiceWithUserModel(service, user)
                } else {
                    null // Jika user tidak ditemukan, layanan ini tidak ditampilkan
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching all services: ${e.message}")
            emptyList()
        }
    }


    suspend fun updateService(serviceId: String, serviceName: String, serviceDesc: String, imageBannerUrl: String, serviceCategory: String, serviceTypes: List<ServiceSelectionModel>, serviceTags: List<String>): Pair<Boolean, String?> {
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
                        "serviceCategory" to serviceCategory,
                        "serviceTypes" to serviceTypes.map { mapOf("name" to it.name, "price" to it.price) },
                        "serviceTags" to serviceTags,
                        "minPrice" to serviceTypes.minOfOrNull { it.price }
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

    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.", ignoreCase = true) == true ->
                "Terjadi kesalahan jaringan, coba lagi nanti"
            else -> e.message ?: "Terjadi kesalahan, coba lagi nanti"
        }
    }
}