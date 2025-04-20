package com.gawebersama.gawekuy.data.repository

import android.util.Log
import com.gawebersama.gawekuy.data.datamodel.TransactionModel
import com.gawebersama.gawekuy.data.enum.OrderStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction.DESCENDING
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val transactionCollection = firestore.collection("transactions")

    private var lastDocument: DocumentSnapshot? = null
    private var isLastPage = false

    companion object {
        const val TAG = "TransactionRepository"
        private const val PAGE_SIZE = 10
    }

    private fun TransactionModel.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "orderId" to orderId,
            "serviceId" to serviceId,
            "serviceName" to serviceName,
            "selectedServiceType" to selectedServiceType,
            "grossAmount" to grossAmount,
            "statusForBuyer" to statusForBuyer,
            "statusForFreelancer" to statusForFreelancer,
            "buyerId" to buyerId,
            "buyerName" to buyerName,
            "buyerEmail" to buyerEmail,
            "buyerPhone" to buyerPhone,
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
            statusForBuyer = get("statusForBuyer") as? String ?: "",
            statusForFreelancer = get("statusForFreelancer") as? String ?: "",
            buyerId = get("buyerId") as? String ?: "",
            buyerName = get("buyerName") as? String ?: "",
            buyerEmail = get("buyerEmail") as? String ?: "",
            buyerPhone = get("buyerPhone") as? String ?: "",
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
        statusForBuyer: String,
        statusForFreelancer: String,
        buyerId: String,
        buyerName: String,
        buyerEmail: String,
        buyerPhone: String,
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
            statusForBuyer = statusForBuyer,
            statusForFreelancer = statusForFreelancer,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerEmail = buyerEmail,
            buyerPhone = buyerPhone,
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

    suspend fun getTransactionByBuyerId(
        buyerId: String,
        filter: String? = null,
        resetPaging: Boolean = false
    ): List<TransactionModel> {
        return try {

            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            if (isLastPage) return emptyList()

            var query = when (filter) {
                OrderStatus.ALL.name -> transactionCollection.whereEqualTo("buyerId", buyerId)
                OrderStatus.REVISION.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Revision")
                OrderStatus.PENDING.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Pending")
                OrderStatus.REJECTED.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Rejected")
                OrderStatus.CANCELLED.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Cancelled")
                OrderStatus.WAITING_RESPONSES.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Waiting Responses")
                OrderStatus.IN_PROGRESS.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "In Progress")
                OrderStatus.COMPLETED.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Completed")
                else -> transactionCollection.whereEqualTo("buyerId", buyerId)
            }.orderBy("transactionTime", DESCENDING).limit(PAGE_SIZE.toLong())

            lastDocument?.let {
                query = query.startAfter(it)
            }

            val querySnapshot = query.get().await()
            val transactions = querySnapshot.documents.mapNotNull { it.data?.toTransaction() }

            lastDocument = querySnapshot.documents.lastOrNull()

            if (querySnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            transactions
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error getting transactions by buyer ID: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTransactionBySellerId(
        sellerId: String,
        filter: String? = null,
        resetPaging: Boolean = false
    ): List<TransactionModel> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            if (isLastPage) return emptyList()

            var query = when (filter) {
                OrderStatus.ALL.name -> transactionCollection.whereEqualTo("sellerId", sellerId)
                OrderStatus.REVISION.name -> transactionCollection.whereEqualTo("sellerId", sellerId).whereEqualTo("statusForFreelancer", "Revision")
                OrderStatus.PENDING.name -> transactionCollection.whereEqualTo("sellerId", sellerId).whereEqualTo("statusForFreelancer", "Pending")
                OrderStatus.REJECTED.name -> transactionCollection.whereEqualTo("sellerId", sellerId).whereEqualTo("statusForFreelancer", "Rejected")
                OrderStatus.CANCELLED.name -> transactionCollection.whereEqualTo("sellerId", sellerId).whereEqualTo("statusForFreelancer", "Cancelled")
                OrderStatus.WAITING_RESPONSES.name -> transactionCollection.whereEqualTo("sellerId", sellerId).whereEqualTo("statusForFreelancer", "Waiting Responses")
                OrderStatus.WAITING_PAYMENT.name -> transactionCollection.whereEqualTo("sellerId", sellerId).whereEqualTo("statusForFreelancer", "Waiting Payment")
                OrderStatus.PAID.name -> transactionCollection.whereEqualTo("sellerId", sellerId).whereEqualTo("statusForFreelancer", "Paid")
                OrderStatus.IN_PROGRESS.name -> transactionCollection.whereEqualTo("sellerId", sellerId).whereEqualTo("statusForFreelancer", "In Progress")
                OrderStatus.COMPLETED.name -> transactionCollection.whereEqualTo("sellerId", sellerId).whereEqualTo("statusForFreelancer", "Completed")
                else -> transactionCollection.whereEqualTo("sellerId", sellerId)
            }.orderBy("transactionTime", DESCENDING).limit(PAGE_SIZE.toLong())

            lastDocument?.let {
                query = query.startAfter(it)
            }

            val querySnapshot = query.get().await()
            val transactions = querySnapshot.documents.mapNotNull { it.data?.toTransaction() }

            lastDocument = querySnapshot.documents.lastOrNull()

            if (querySnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            transactions
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error getting transactions by seller ID: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllTransaction(
        filter: String?,
        resetPaging: Boolean = false
    ): List<TransactionModel> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            if (isLastPage) return emptyList()

            var query = when (filter) {
                OrderStatus.WAITING_PAYMENT.name -> transactionCollection.whereEqualTo("statusForFreelancer", "Waiting Payment")
                OrderStatus.PAID.name -> transactionCollection.whereEqualTo("statusForFreelancer", "Paid")
                else -> transactionCollection
            }.orderBy("transactionTime", DESCENDING).limit(PAGE_SIZE.toLong())

            lastDocument?.let {
                query = query.startAfter(it)
            }

            val querySnapshot = query.get().await()
            val transactions = querySnapshot.documents.mapNotNull { it.data?.toTransaction() }

            lastDocument = querySnapshot.documents.lastOrNull()

            if (querySnapshot.documents.size < PAGE_SIZE) {
                isLastPage = true
            }

            transactions
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error getting all transactions: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateTransactionStatusForBuyer(transactionId: String, newStatusForBuyer: String): Pair<Boolean, String?> {
        return try {
            transactionCollection.document(transactionId).update("statusForBuyer", newStatusForBuyer).await()

            Log.d(TAG, "Transaction status updated with ID: $transactionId")
            Pair(true, "Status transaksi berhasil diperbarui")
            } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error updating transaction status: ${e.message}")
            Pair(false, getErrorMessage(e))
        }
    }

    suspend fun updateTransactionStatusForFreelancer(transactionId: String, newStatusForFreelancer: String): Pair<Boolean, String?> {
        return try {
            transactionCollection.document(transactionId).update("statusForFreelancer", newStatusForFreelancer).await()

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

    fun isLastPage(): Boolean {
        return isLastPage
    }
}