package com.gawebersama.gawekuy.data.repository

import android.util.Log
import com.gawebersama.gawekuy.data.datamodel.PortfolioModel
import com.gawebersama.gawekuy.data.datamodel.ServiceModel
import com.gawebersama.gawekuy.data.datamodel.ServiceSelectionModel
import com.gawebersama.gawekuy.data.datamodel.ServiceWithUserModel
import com.gawebersama.gawekuy.data.datamodel.UserModel
import com.gawebersama.gawekuy.data.enum.FilterAndOrderService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction.ASCENDING
import com.google.firebase.firestore.Query.Direction.DESCENDING
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class ServiceRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "ServiceRepository"
        private const val PAGE_SIZE = 10
    }

    private var lastDocument: DocumentSnapshot? = null
    private var isLastPage = false

    private val serviceCollection = firestore.collection("services")
    private val userCollection = firestore.collection("users")

    private fun ServiceModel.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "serviceId" to serviceId,
            "userId" to userId,
            "serviceName" to serviceName,
            "serviceDesc" to serviceDesc,
            "imageBannerUrl" to imageBannerUrl,
            "serviceOrdered" to serviceOrdered,
            "serviceTypes" to serviceTypes,
            "minPrice" to minPrice,
            "serviceCategory" to serviceCategory,
            "serviceTags" to serviceTags,
            "portfolio" to portfolio,
            "createdAt" to createdAt
        )
    }

    private fun Map<String, Any>.toService(): ServiceModel {
        return ServiceModel(
            serviceId = this["serviceId"] as? String ?: "",
            userId = this["userId"] as? String ?: "",
            serviceName = this["serviceName"] as? String ?: "",
            serviceDesc = this["serviceDesc"] as? String,
            imageBannerUrl = this["imageBannerUrl"] as? String,
            serviceOrdered = (this["serviceOrdered"] as? Number)?.toInt() ?: 0,
            serviceTypes = (this["serviceTypes"] as? List<*>?)?.mapNotNull { it as? Map<*, *> }?.map {
                ServiceSelectionModel(
                    name = it["name"] as? String ?: "",
                    price = (it["price"] as? Number)?.toDouble() ?: 0.0
                )
            } ?: emptyList(),
            minPrice = (this["minPrice"] as? Number)?.toDouble() ?: 0.0,
            serviceCategory = this["serviceCategory"] as? String ?: "",
            serviceTags = (this["serviceTags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            portfolio = (this["portfolio"] as? List<*>)?.filterIsInstance<Map<String, String>>() ?: emptyList(),
            createdAt = this["createdAt"] as? Timestamp ?: Timestamp.now()
        )
    }

    suspend fun createService(
        serviceName: String,
        serviceDesc: String,
        imageBannerUrl: String,
        serviceCategory: String,
        serviceTypes: List<ServiceSelectionModel>,
        serviceTags: List<String>,
        portfolio: List<Map<String, String>>
    ): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val serviceId = serviceCollection.document().id // Auto-generate ID

            val minPrice = serviceTypes.minOfOrNull { it.price.toDouble() } ?: 0.0

            val serviceModelData = ServiceModel(
                serviceId = serviceId,
                userId = userId,
                serviceName = serviceName,
                serviceDesc = serviceDesc,
                imageBannerUrl = imageBannerUrl,
                serviceOrdered = 0,
                serviceTypes = serviceTypes,
                minPrice = minPrice,
                serviceCategory = serviceCategory,
                serviceTags = serviceTags,
                portfolio = portfolio,
                createdAt = Timestamp.now()
            ).toHashMap()

            serviceCollection.document(serviceId).set(serviceModelData).await()
            Log.d(TAG, "Service created with ID: $serviceId")
            Pair(true, "Jasa berhasil dibuat")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error creating service: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }


    suspend fun getUserServices(resetPaging: Boolean = false): List<ServiceWithUserModel> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            val userId = firebaseAuth.currentUser?.uid ?: return emptyList()

            if (isLastPage) return emptyList()

            var query: Query = serviceCollection
                .whereEqualTo("userId", userId)
                .limit(PAGE_SIZE.toLong())

            // Jika ada lastDocument, gunakan startAfter untuk melanjutkan paging
            lastDocument?.let {
                query = query.startAfter(it)
            }

            // Ambil data layanan milik user
            val serviceSnapshot = query.get().await()
            val services = serviceSnapshot.documents.mapNotNull { it.data?.toService() }

            // Simpan dokumen terakhir untuk paginasi berikutnya
            lastDocument = serviceSnapshot.documents.lastOrNull()

            // Cek apakah ini halaman terakhir
            if (serviceSnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            // Ambil data user yang sedang login
            val userSnapshot = userCollection
                .document(userId)
                .get()
                .await()

            val userData = userSnapshot.toObject(UserModel::class.java)

            if (userData == null) {
                Log.e(TAG, "User data not found for userId: $userId")
                return emptyList()
            }

            // Gabungkan layanan dengan data pengguna
            services.map { service -> ServiceWithUserModel(service, userData, emptyList()) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user services: ${e.message}")
            emptyList()
        }
    }

    suspend fun searchService(filter: FilterAndOrderService? = null, query: String, resetPaging: Boolean = false): List<ServiceWithUserModel> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            if (isLastPage || query.isBlank()) return emptyList()

            // Apply filter for service category
            var collectionService = when (filter) {
                FilterAndOrderService.ACADEMIC_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Penulisan & Akademik")
                FilterAndOrderService.ACADEMIC_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Penulisan & Akademik")
                FilterAndOrderService.TECH_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Pengembangan Teknologi")
                FilterAndOrderService.TECH_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Pengembangan Teknologi")
                FilterAndOrderService.DESIGN_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Desain & Multimedia")
                FilterAndOrderService.DESIGN_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Desain & Multimedia")
                FilterAndOrderService.MARKETING_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Pemasaran & Media Sosial")
                FilterAndOrderService.MARKETING_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Pemasaran & Media Sosial")
                FilterAndOrderService.OTHERS_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Lainnya")
                FilterAndOrderService.OTHERS_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Lainnya")
                else -> serviceCollection
            }.limit(PAGE_SIZE.toLong())

            // Build search query
            var serviceQuery = collectionService
                .orderBy("serviceName")  // Only works for exact matches on prefixes
                .startAt(query)          // Start from the query text
                .endAt(query + "\uf8ff") // End with all possible characters after query text

            // Apply pagination if lastDocument exists
            lastDocument?.let {
                serviceQuery = serviceQuery.startAfter(it)
            }

            // Execute query to get the data
            val serviceSnapshot = serviceQuery.get().await()

            if (serviceSnapshot.isEmpty) {
                isLastPage = true
                return emptyList()
            }

            // Extract service data
            val services = serviceSnapshot.documents.mapNotNull { it.data?.toService() }

            // Set the last document for pagination
            lastDocument = serviceSnapshot.documents.lastOrNull()

            // Check if this is the last page
            isLastPage = serviceSnapshot.documents.size < PAGE_SIZE

            val userIds = services.map { it.userId }.toSet()
            if (userIds.isEmpty()) return emptyList()

            val userMap = fetchUsersByIds(userIds)

            Log.d(TAG, "Last document: ${lastDocument?.id}")
            Log.d(TAG, "Total fetched services: ${services.size}")

            // Combine services with user data
            return services.mapNotNull { service ->
                val user = userMap[service.userId]
                if (user != null) {
                    ServiceWithUserModel(service, user)
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching services: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun getServiceById(serviceId: String): ServiceWithUserModel? {
        return try {
            val serviceSnapshot = serviceCollection.document(serviceId).get().await()
            val service = serviceSnapshot.toObject(ServiceModel::class.java) ?: return null

            val userSnapshot = userCollection.document(service.userId).get().await()
            val user = userSnapshot.toObject(UserModel::class.java) ?: return null

            val portfolios = getPortfolioByServiceId(serviceId)

            ServiceWithUserModel(service, user, portfolios)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching service by ID: ${e.message}")
            null
        }
    }

    suspend fun getAllService(
        filter: FilterAndOrderService? = null,
        resetPaging: Boolean = false
    ): List<ServiceWithUserModel> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            if (isLastPage) return emptyList()

            var query: Query = when (filter) {
                FilterAndOrderService.CHEAP -> serviceCollection.orderBy("minPrice", ASCENDING)
                FilterAndOrderService.EXPENSIVE -> serviceCollection.orderBy("minPrice", DESCENDING)
                FilterAndOrderService.ORDERED -> serviceCollection.orderBy("serviceOrdered", DESCENDING)
                FilterAndOrderService.ACADEMIC_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Penulisan & Akademik").orderBy("minPrice", ASCENDING)
                FilterAndOrderService.ACADEMIC_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Penulisan & Akademik").orderBy("minPrice", DESCENDING)
                FilterAndOrderService.TECH_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Pengembangan Teknologi").orderBy("minPrice", ASCENDING)
                FilterAndOrderService.TECH_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Pengembangan Teknologi").orderBy("minPrice", DESCENDING)
                FilterAndOrderService.DESIGN_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Desain & Multimedia").orderBy("minPrice", ASCENDING)
                FilterAndOrderService.DESIGN_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Desain & Multimedia").orderBy("minPrice", DESCENDING)
                FilterAndOrderService.MARKETING_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Pemasaran & Media Sosial").orderBy("minPrice", ASCENDING)
                FilterAndOrderService.MARKETING_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Pemasaran & Media Sosial").orderBy("minPrice", DESCENDING)
                FilterAndOrderService.OTHERS_CHEAP -> serviceCollection.whereEqualTo("serviceCategory", "Lainnya").orderBy("minPrice", ASCENDING)
                FilterAndOrderService.OTHERS_EXPENSIVE -> serviceCollection.whereEqualTo("serviceCategory", "Lainnya").orderBy("minPrice", DESCENDING)
                null -> serviceCollection
            }.limit(PAGE_SIZE.toLong())

            // Jika ada lastDocument, gunakan startAfter untuk melanjutkan paging
            lastDocument?.let {
                query = query.startAfter(it)
            }

            val serviceSnapshot = query.get().await()

            // Jika tidak ada data, langsung set isLastPage = true
            if (serviceSnapshot.isEmpty) {
                isLastPage = true
                return emptyList()
            }

            val services = serviceSnapshot.documents.mapNotNull { it.data?.toService() }

            // Simpan dokumen terakhir untuk paginasi berikutnya
            if (serviceSnapshot.documents.isNotEmpty()) {
                lastDocument = serviceSnapshot.documents.last()
            } else {
                isLastPage = true // Jika tidak ada dokumen lagi, tandai sebagai halaman terakhir
            }

            // Cek apakah ini halaman terakhir
            if (serviceSnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            val userIds = services.map { it.userId }.toSet()
            if (userIds.isEmpty()) return emptyList()

            val userMap = fetchUsersByIds(userIds)

            // Gabungkan layanan dengan data pengguna
            return services.mapNotNull { service ->
                val user = userMap[service.userId]
                if (user != null) {
                    ServiceWithUserModel(service, user)
                } else null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirestorePaging", "Error fetching paged services: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPortfolioByServiceId(serviceId: String): List<PortfolioModel> {
        return try {
            val serviceSnapshot = serviceCollection.document(serviceId).get().await()
            val service = serviceSnapshot.toObject(ServiceModel::class.java) ?: return emptyList()
            val portfolioIds = service.portfolio.mapNotNull { it["portfolioId"] }

            if (portfolioIds.isEmpty()) return emptyList()

            val portfolios = mutableListOf<PortfolioModel>()

            portfolioIds.chunked(10).forEach { batch ->
                val portfolioSnapshot = firestore.collection("portfolios")
                    .whereIn("portfolioId", batch)
                    .get()
                    .await()

                portfolios.addAll(portfolioSnapshot.documents.mapNotNull { it.toObject(PortfolioModel::class.java) })
            }

            return portfolios

            } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching portfolio by service ID: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateService(
        serviceId: String,
        serviceName: String,
        serviceDesc: String,
        imageBannerUrl: String,
        serviceCategory: String,
        serviceTypes: List<ServiceSelectionModel>,
        serviceTags: List<String>,
        portfolio: List<Map<String, String>>
    ): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val serviceRef = serviceCollection.document(serviceId)
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
                        "portfolio" to portfolio,
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

    suspend fun updateServiceOrdered(serviceId: String, serviceOrdered: Int): Pair<Boolean, String?> {
        return try {
            val serviceRef = serviceCollection.document(serviceId)
            val serviceSnapshot = serviceRef.get().await()

            if (serviceSnapshot.exists()) {
                serviceRef.update("serviceOrdered", serviceOrdered).await()
            } else {
                return Pair(false, "Jasa tidak ditemukan")
            }

            Pair(true, "Jasa berhasil diperbarui")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun deleteService(serviceId: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val serviceRef = serviceCollection.document(serviceId)
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

    fun isLastPage(): Boolean {
        return isLastPage
    }

    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.", ignoreCase = true) == true ->
                "Terjadi kesalahan jaringan, coba lagi nanti"
            else -> e.message ?: "Terjadi kesalahan, coba lagi nanti"
        }
    }

    private suspend fun fetchUsersByIds(userIds: Set<String>): Map<String, UserModel> {
        val result = mutableMapOf<String, UserModel>()
        userIds.chunked(10).forEach { chunk ->
            val snapshot = firestore.collection("users")
                .whereIn("userId", chunk)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { it.toObject(UserModel::class.java) }
            result.putAll(users.filter { it.accountStatus == true }.associateBy { it.userId })
        }
        return result
    }
}