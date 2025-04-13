package com.gawebersama.gawekuy.data.repository

import android.util.Log
import com.gawebersama.gawekuy.data.datamodel.TransactionModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val transactionCollection = firestore.collection("transactions")

    companion object {
        const val TAG = "TransactionRepository"
    }

    private fun TransactionModel.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "orderId" to orderId,
            "serviceId" to serviceId,
            "serviceName" to serviceName,
            "selectedServiceType" to selectedServiceType,
            "grossAmount" to grossAmount,
            "status" to status,
            "buyerId" to buyerId,
            "buyerName" to buyerName,
            "buyerEmail" to buyerEmail,
            "sellerId" to sellerId,
            "sellerName" to sellerName,
            "sellerEmail" to sellerEmail,
            "sellerPhone" to sellerPhone,
            "transactionTime" to transactionTime
        )
    }

    private fun Map<String, Any>.toTransaction(): TransactionModel {
        return TransactionModel(
            orderId = get("orderId") as? String ?: "",
            serviceId = get("serviceId") as? String,
            serviceName = get("serviceName") as? String,
            selectedServiceType = get("selectedServiceType") as? String,
            grossAmount = (get("grossAmount") as? Number)?.toInt() ?: 0,
            status = get("status") as? String ?: "",
            buyerId = get("buyerId") as? String ?: "",
            buyerName = get("buyerName") as? String ?: "",
            buyerEmail = get("buyerEmail") as? String ?: "",
            sellerId = get("sellerId") as? String ?: "",
            sellerName = get("sellerName") as? String ?: "",
            sellerEmail = get("sellerEmail") as? String ?: "",
            sellerPhone = get("sellerPhone") as? String ?: "",
            transactionTime = get("transactionTime") as? Timestamp ?: Timestamp.now()
        )
    }

    suspend fun saveTransaction(
        orderId: String,
        serviceId: String?,
        serviceName: String?,
        selectedServiceType: String?,
        grossAmount: Int,
        status: String,
        buyerId: String,
        buyerName: String,
        buyerEmail: String,
        sellerId: String,
        sellerName: String,
        sellerEmail: String,
        sellerPhone: String,
        transactionTime: Timestamp
    ) : Pair<Boolean, String?> {
        val transactionModelData = TransactionModel(
            orderId = orderId,
            serviceId = serviceId,
            serviceName = serviceName,
            selectedServiceType = selectedServiceType,
            grossAmount = grossAmount,
            status = status,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerEmail = buyerEmail,
            sellerId = sellerId,
            sellerName = sellerName,
            sellerEmail = sellerEmail,
            sellerPhone = sellerPhone,
            transactionTime = transactionTime
        ).toHashMap()

        return try {
            transactionCollection.document(orderId).set(transactionModelData).await()
            Log.d(TAG, "Transaction created with ID: $orderId")
            Pair(true, "Transaksi berhasil dibuat")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error creating transaction: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun getTransactionByBuyerId(buyerId: String): List<TransactionModel> {
        return try {
            val querySnapshot = transactionCollection
                .whereEqualTo("buyerId", buyerId)
                .get()
                .await()
            querySnapshot.documents.mapNotNull { it.data?.toTransaction() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error getting transactions by buyer ID: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTransactionBySellerId(sellerId: String): List<TransactionModel> {
        return try {
            val querySnapshot = transactionCollection
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()
            querySnapshot.documents.mapNotNull { it.data?.toTransaction() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error getting transactions by seller ID: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTransactionById(transactionId: String): TransactionModel? {
        return try {
            val documentSnapshot = transactionCollection.document(transactionId).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.data?.toTransaction()
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error getting transaction by ID: ${e.message}")
            null
        }
    }

    suspend fun updateTransactionStatus(transactionId: String, newStatus: String): Pair<Boolean, String?> {
        return try {
            transactionCollection.document(transactionId).update("status", newStatus).await()
            Log.d(TAG, "Transaction status updated with ID: $transactionId")
            Pair(true, "Status transaksi berhasil diperbarui")
            } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error updating transaction status: ${e.message}")
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