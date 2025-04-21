package com.gawebersama.gawekuy.data.repository

import android.util.Log
import com.gawebersama.gawekuy.data.datamodel.ServiceModel
import com.gawebersama.gawekuy.data.datamodel.TransactionDetailModel
import com.gawebersama.gawekuy.data.datamodel.TransactionModel
import com.gawebersama.gawekuy.data.datamodel.UserModel
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
    private var lastVisibleTime: Timestamp? = null

    companion object {
        const val TAG = "TransactionRepository"
        private const val PAGE_SIZE = 10
    }

    private fun TransactionModel.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "orderId" to orderId,
            "serviceId" to serviceId,
            "selectedServiceType" to selectedServiceType,
            "selectedServicePrice" to selectedServicePrice,
            "grossAmount" to grossAmount,
            "statusForBuyer" to statusForBuyer,
            "statusForFreelancer" to statusForFreelancer,
            "buyerId" to buyerId,
            "sellerId" to sellerId,
            "transactionTime" to transactionTime
        )
    }

    private fun Map<String, Any>.toTransaction(): TransactionModel {
        return TransactionModel(
            orderId = get("orderId") as? String ?: "",
            serviceId = get("serviceId") as? String,
            selectedServiceType = get("selectedServiceType") as? String,
            selectedServicePrice = (get("selectedServicePrice") as? Number)?.toInt() ?: 0,
            grossAmount = (get("grossAmount") as? Number)?.toInt() ?: 0,
            statusForBuyer = get("statusForBuyer") as? String ?: "",
            statusForFreelancer = get("statusForFreelancer") as? String ?: "",
            buyerId = get("buyerId") as? String ?: "",
            sellerId = get("sellerId") as? String ?: "",
            transactionTime = get("transactionTime") as? Timestamp ?: Timestamp.now()
        )
    }

    suspend fun saveTransaction(
        orderId: String,
        serviceId: String?,
        selectedServiceType: String?,
        selectedServicePrice: Int,
        grossAmount: Int,
        statusForBuyer: String,
        statusForFreelancer: String,
        buyerId: String,
        sellerId: String,
        transactionTime: Timestamp
    ) : Pair<Boolean, String?> {
        val transactionModelData = TransactionModel(
            orderId = orderId,
            serviceId = serviceId,
            selectedServiceType = selectedServiceType,
            selectedServicePrice = selectedServicePrice,
            grossAmount = grossAmount,
            statusForBuyer = statusForBuyer,
            statusForFreelancer = statusForFreelancer,
            buyerId = buyerId,
            sellerId = sellerId,
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
    ): List<TransactionDetailModel> {
        return try {
            if (resetPaging) {
                lastDocument = null
                isLastPage = false
            }

            if (isLastPage) return emptyList()

            // Query transaksi berdasarkan filter
            var query = when (filter) {
                OrderStatus.ALL.name -> transactionCollection.whereEqualTo("buyerId", buyerId)
                OrderStatus.REVISION.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Revision")
                OrderStatus.PENDING.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Pending")
                OrderStatus.REJECTED.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Rejected")
                OrderStatus.CANCELLED.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Cancelled")
                OrderStatus.WAITING_RESPONSES.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Waiting Responses")
                OrderStatus.WAITING_REFUND.name -> transactionCollection.whereEqualTo("buyerId", buyerId).whereEqualTo("statusForBuyer", "Waiting Refund")
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

            if (transactions.isEmpty()) return emptyList()

            // Ambil semua serviceId, buyerId, sellerId
            val serviceIds = transactions.mapNotNull { it.serviceId }.toSet()
            val buyerIds = transactions.map { it.buyerId }.toSet()
            val sellerIds = transactions.map { it.sellerId }.toSet()

            val serviceMap = fetchServicesByIds(serviceIds)
            val buyerMap = fetchUsersByIds(buyerIds)
            val sellerMap = fetchUsersByIds(sellerIds)

            return transactions.mapNotNull { transaction ->
                val service = serviceMap[transaction.serviceId]
                val buyer = buyerMap[transaction.buyerId]
                val seller = sellerMap[transaction.sellerId]

                if (service != null && buyer != null && seller != null) {
                    TransactionDetailModel(transaction, service, buyer, seller)
                } else null
            }

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
    ): List<TransactionDetailModel> {
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

            if (transactions.isEmpty()) return emptyList()

            // Ambil semua serviceId, buyerId, sellerId
            val serviceIds = transactions.mapNotNull { it.serviceId }.toSet()
            val buyerIds = transactions.map { it.buyerId }.toSet()
            val sellerIds = transactions.map { it.sellerId }.toSet()

            val serviceMap = fetchServicesByIds(serviceIds)
            val buyerMap = fetchUsersByIds(buyerIds)
            val sellerMap = fetchUsersByIds(sellerIds)

            return transactions.mapNotNull { transaction ->
                val service = serviceMap[transaction.serviceId]
                val buyer = buyerMap[transaction.buyerId]
                val seller = sellerMap[transaction.sellerId]

                if (service != null && buyer != null && seller != null) {
                    TransactionDetailModel(transaction, service, buyer, seller)
                } else null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error getting transactions by seller ID: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllTransaction(
        filter: String?,
        resetPaging: Boolean = false
    ): List<TransactionDetailModel> {
        return try {
            if (resetPaging) {
                lastVisibleTime = null
                isLastPage = false
            }

            if (isLastPage) return emptyList()

            val queries = when (filter) {
                OrderStatus.WAITING_REFUND.name -> listOf(
                    transactionCollection.whereEqualTo("statusForBuyer", "Waiting Refund")
                )
                OrderStatus.CANCELLED.name -> listOf(
                    transactionCollection.whereEqualTo("statusForBuyer", "Cancelled")
                )
                OrderStatus.WAITING_PAYMENT.name -> listOf(
                    transactionCollection.whereEqualTo("statusForFreelancer", "Waiting Payment")
                )
                OrderStatus.PAID.name -> listOf(
                    transactionCollection.whereEqualTo("statusForFreelancer", "Paid")
                )
                else -> listOf(
                    transactionCollection.whereEqualTo("statusForBuyer", "Waiting Refund"),
                    transactionCollection.whereEqualTo("statusForBuyer", "Cancelled"),
                    transactionCollection.whereEqualTo("statusForFreelancer", "Waiting Payment"),
                    transactionCollection.whereEqualTo("statusForFreelancer", "Paid")
                )
            }

            val allTransactions = mutableListOf<TransactionModel>()

            for (query in queries) {
                var pagedQuery = query.orderBy("transactionTime", DESCENDING).limit(PAGE_SIZE.toLong() * 2)

                lastVisibleTime?.let { timestamp ->
                    pagedQuery = pagedQuery.whereLessThan("transactionTime", timestamp)
                }

                val snapshot = pagedQuery.get().await()
                val transactions = snapshot.documents.mapNotNull { it.data?.toTransaction() }
                allTransactions.addAll(transactions)
            }

            val sorted = allTransactions
                .distinctBy { it.orderId }
                .sortedByDescending { it.transactionTime }
                .take(PAGE_SIZE)

            lastVisibleTime = sorted.lastOrNull()?.transactionTime

            if (sorted.size < PAGE_SIZE) {
                isLastPage = true
            }

            if (sorted.isEmpty()) return emptyList()

            // Enrich data
            val serviceIds = sorted.mapNotNull { it.serviceId }.toSet()
            val buyerIds = sorted.map { it.buyerId }.toSet()
            val sellerIds = sorted.map { it.sellerId }.toSet()

            val serviceMap = fetchServicesByIds(serviceIds)
            val buyerMap = fetchUsersByIds(buyerIds)
            val sellerMap = fetchUsersByIds(sellerIds)

            return sorted.mapNotNull { transaction ->
                val service = serviceMap[transaction.serviceId]
                val buyer = buyerMap[transaction.buyerId]
                val seller = sellerMap[transaction.sellerId]

                if (service != null && buyer != null && seller != null) {
                    TransactionDetailModel(transaction, service, buyer, seller)
                } else null
            }
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

    private suspend fun fetchServicesByIds(serviceIds: Set<String>): Map<String, ServiceModel> {
        val result = mutableMapOf<String, ServiceModel>()
        serviceIds.chunked(10).forEach { chunk ->
            val snapshot = firestore.collection("services")
                .whereIn("serviceId", chunk)
                .get()
                .await()

            val services = snapshot.documents.mapNotNull { it.toObject(ServiceModel::class.java) }
            result.putAll(services.associateBy { it.serviceId })
        }
        return result
    }

    private suspend fun fetchUsersByIds(userIds: Set<String>): Map<String, UserModel> {
        val result = mutableMapOf<String, UserModel>()
        userIds.chunked(10).forEach { chunk ->
            val snapshot = firestore.collection("users")
                .whereIn("userId", chunk)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { it.toObject(UserModel::class.java) }
            result.putAll(users.associateBy { it.userId })
        }
        return result
    }
}