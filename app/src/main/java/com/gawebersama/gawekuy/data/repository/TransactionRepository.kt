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
            "grossAmount" to grossAmount,
            "status" to status,
            "buyerId" to buyerId,
            "buyerName" to buyerName,
            "buyerEmail" to buyerEmail,
            "sellerId" to sellerId,
            "sellerName" to sellerName,
            "sellerEmail" to sellerEmail,
            "transactionTime" to transactionTime
        )
    }

    private fun Map<String, Any>.toTransaction(): TransactionModel {
        return TransactionModel(
            orderId = this["orderId"] as? String ?: "",
            serviceId = this["serviceId"] as? String ?: "",
            grossAmount = (this["grossAmount"] as? Number)?.toDouble() ?: 0.0,
            status = this["status"] as? String ?: "",
            buyerId = this["buyerId"] as? String ?: "",
            buyerName = this["buyerName"] as? String ?: "",
            buyerEmail = this["buyerEmail"] as? String ?: "",
            sellerId = this["sellerId"] as? String ?: "",
            sellerName = this["sellerName"] as? String ?: "",
            sellerEmail = this["sellerEmail"] as? String ?: "",
            transactionTime = this["transactionTime"] as? Timestamp ?: Timestamp.now()
        )
    }

    suspend fun saveTransaction(
        orderId: String,
        serviceId: String?,
        grossAmount: Double,
        status: String,
        buyerId: String,
        buyerName: String,
        buyerEmail: String,
        sellerId: String,
        sellerName: String,
        sellerEmail: String,
        transactionTime: Timestamp
    ) : Pair<Boolean, String?> {
        val transactionModelData = TransactionModel(
            orderId = orderId,
            serviceId = serviceId,
            grossAmount = grossAmount,
            status = status,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerEmail = buyerEmail,
            sellerId = sellerId,
            sellerName = sellerName,
            sellerEmail = sellerEmail,
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

    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.", ignoreCase = true) == true ->
                "Terjadi kesalahan jaringan, coba lagi nanti"
            else -> e.message ?: "Terjadi kesalahan, coba lagi nanti"
        }
    }
}