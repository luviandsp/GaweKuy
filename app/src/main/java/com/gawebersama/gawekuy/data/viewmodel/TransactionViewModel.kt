package com.gawebersama.gawekuy.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gawebersama.gawekuy.data.datamodel.TransactionDetailModel
import com.gawebersama.gawekuy.data.repository.TransactionRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class TransactionViewModel: ViewModel() {

    private val transactionRepository = TransactionRepository()

    private val _transactions = MutableLiveData<List<TransactionDetailModel>>()
    val transactions: LiveData<List<TransactionDetailModel>> get() = _transactions

    private val _operationResult = MutableLiveData<Pair<Boolean, String?>>()
    val operationResult: LiveData<Pair<Boolean, String?>> get() = _operationResult

    private val _transactionList = MutableLiveData<List<TransactionDetailModel>>()
    val transactionList: LiveData<List<TransactionDetailModel>> get() = _transactionList

    private var currentList = mutableListOf<TransactionDetailModel>()

    fun hasMoreData(): Boolean {
        return !transactionRepository.isLastPage()
    }

    companion object {
        const val TAG = "TransactionViewModel"
    }

    fun saveTransaction(
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
    ) {
        viewModelScope.launch {
            val result = transactionRepository.saveTransaction(
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
            )

            _operationResult.value = result
        }
    }

    fun getAllTransactionsByBuyerId(
        buyerId: String,
        filter: String? = null,
        resetPaging: Boolean = false
    ) {
        viewModelScope.launch {
            val transactions = transactionRepository.getTransactionByBuyerId(buyerId, filter, resetPaging)
            Log.d(TAG, "Buyer Transaction List: $transactions")

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(transactions)
            _transactionList.postValue(currentList)
            Log.d(TAG, "Current list size: ${currentList.size}")
        }
    }

    fun getAllTransactionsBySellerId(
        sellerId: String,
        filter: String? = null,
        resetPaging: Boolean = false
    ) {
        viewModelScope.launch {
            val transactions = transactionRepository.getTransactionBySellerId(sellerId, filter, resetPaging)
            Log.d(TAG, "Seller Transaction List: $transactions")

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(transactions)
            _transactionList.postValue(currentList)
            Log.d(TAG, "Current list size: ${currentList.size}")
        }
    }

    fun updateTransactionStatusForBuyer(
        transactionId: String,
        newStatusForBuyer: String
    ) {
        viewModelScope.launch {
            val result = transactionRepository.updateTransactionStatusForBuyer(transactionId, newStatusForBuyer)
            _operationResult.postValue(result)
        }
    }

    fun updateTransactionStatusForFreelancer(
        transactionId: String,
        newStatusForFreelancer: String
    ) {
        viewModelScope.launch {
            val result = transactionRepository.updateTransactionStatusForFreelancer(transactionId, newStatusForFreelancer)
            _operationResult.postValue(result)
        }
    }

    fun getAllTransactions(
        filter: String? = null,
        resetPaging: Boolean = false
    ) {
        viewModelScope.launch {
            val transactions = transactionRepository.getAllTransaction(filter, resetPaging)
            Log.d(TAG, "All Transaction List: $transactions")

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(transactions)
            _transactionList.postValue(currentList)
            Log.d(TAG, "Current list size: ${currentList.size}")
        }
    }

}