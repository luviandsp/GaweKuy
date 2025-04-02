package com.gawebersama.gawekuy.data.repository

import android.util.Log
import com.gawebersama.gawekuy.data.datamodel.PortfolioModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class PortfolioRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "PortfolioRepository"
    }

    private fun PortfolioModel.toHashMap() : HashMap<String, Any?> {
        return hashMapOf(
            "portfolioId" to portfolioId,
            "userId" to userId,
            "portfolioTitle" to portfolioTitle,
            "portfolioDesc" to portfolioDesc,
            "portfolioBannerImage" to portfolioBannerImage
//            "portfolioImageUrl" to portfolioImageUrl
        )
    }

    private fun Map<String, Any>.toPortfolio() : PortfolioModel {
        return PortfolioModel(
            portfolioId = this["portfolioId"] as String,
            userId = this["userId"] as String,
            portfolioTitle = this["portfolioTitle"] as String,
            portfolioDesc = this["portfolioDesc"] as? String,
            portfolioBannerImage = this["portfolioBannerImage"] as? String
//            portfolioImageUrl = (this["portfolioImageUrl"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )
    }

    suspend fun createPortfolio(
        portfolioTitle: String,
        portfolioDesc: String,
        portfolioBannerImage : String,
//        portfolioImageUrl: List<String>
    ): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val portfolioId = firestore.collection("portfolios").document().id // Auto-generate ID

            val portfolioModelData = PortfolioModel(
                portfolioId = portfolioId,
                userId = userId,
                portfolioTitle = portfolioTitle,
                portfolioDesc = portfolioDesc,
                portfolioBannerImage = portfolioBannerImage
//                portfolioImageUrl = portfolioImageUrl
            ).toHashMap()

            firestore.collection("portfolios").document(portfolioId).set(portfolioModelData).await()
            Log.d(TAG, "Portfolio created with ID: $portfolioId")
            Pair(true, "Portfolio berhasil dibuat")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error creating portfolio: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun getUserPortfolios(userId : String): List<PortfolioModel> {
        return try {

            // Ambil semua portfolio milik user yang sedang login
            val portfolioSnapshot = firestore.collection("portfolios")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            portfolioSnapshot.documents.mapNotNull { it.data?.toPortfolio() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching user portfolios: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPortfolioById(portfolioId: String): PortfolioModel? {
        return try {
            val portfolioSnapshot = firestore.collection("portfolios").document(portfolioId).get().await()
            portfolioSnapshot.toObject(PortfolioModel::class.java)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error fetching portfolio by ID: ${e.message}")
            null
        }
    }

    suspend fun updatePortfolio(
        portfolioId: String,
        portfolioTitle: String,
        portfolioDesc: String,
        portfolioBannerImage: String
//        portfolioImageUrl: List<String>
    ): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val portfolioRef = firestore.collection("portfolios").document(portfolioId)
            val portfolioSnapshot = portfolioRef.get().await()

            if (portfolioSnapshot.exists() && portfolioSnapshot.getString("userId") == userId) {
                portfolioRef.update(
                    mapOf(
                        "portfolioTitle" to portfolioTitle,
                        "portfolioDesc" to portfolioDesc,
                        "portfolioBannerImage" to portfolioBannerImage
//                        "portfolioImageUrl" to portfolioImageUrl
                    )
                ).await()
                Pair(true, "Portfolio berhasil diperbarui")
            } else {
                Pair(false, "Anda tidak berhak mengupdate portfolio ini")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun deletePortfolio(portfolioId: String): Pair<Boolean, String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Pair(false, "User belum login")
            val portfolioRef = firestore.collection("portfolios").document(portfolioId)
            val portfolioSnapshot = portfolioRef.get().await()

            if (portfolioSnapshot.exists() && portfolioSnapshot.getString("userId") == userId) {
                portfolioRef.delete().await()
            } else {
                return Pair(false, "Anda tidak berhak menghapus portfolio ini")
            }

            Log.d(TAG, "Portfolio deleted successfully")
            Pair(true, "Portfolio berhasil dihapus")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error deleting portfolio: ${e.message}")
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