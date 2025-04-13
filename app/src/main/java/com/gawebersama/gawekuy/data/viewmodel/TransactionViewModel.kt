package com.gawebersama.gawekuy.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gawebersama.gawekuy.data.datamodel.TransactionModel
import com.gawebersama.gawekuy.data.repository.TransactionRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class TransactionViewModel: ViewModel() {

    private val transactionRepository = TransactionRepository()

    private val _transactions = MutableLiveData<List<TransactionModel>>()
    val transactions: LiveData<List<TransactionModel>> get() = _transactions

    private val _operationResult = MutableLiveData<Pair<Boolean, String?>>()
    val operationResult: LiveData<Pair<Boolean, String?>> get() = _operationResult

    private val _transactionList = MutableLiveData<List<TransactionModel>>()
    val transactionList: LiveData<List<TransactionModel>> get() = _transactionList

    companion object {
        const val TAG = "TransactionViewModel"
    }

    fun saveTransaction(
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
    ) {
        viewModelScope.launch {
            val result = transactionRepository.saveTransaction(
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
            )

            _operationResult.value = result
        }
    }

    fun getAllTransactionsByBuyerId(
        buyerId: String
    ) {
        viewModelScope.launch {
            val transactions = transactionRepository.getTransactionByBuyerId(buyerId)
            Log.d(TAG, "Buyer Transaction List: $transactions")
            _transactionList.value = transactions
        }
    }

    fun getAllTransactionsBySellerId(
        sellerId: String
    ) {
        viewModelScope.launch {
            val transactions = transactionRepository.getTransactionBySellerId(sellerId)
            Log.d(TAG, "Seller Transaction List: $transactions")
            _transactionList.value = transactions
        }
    }
}