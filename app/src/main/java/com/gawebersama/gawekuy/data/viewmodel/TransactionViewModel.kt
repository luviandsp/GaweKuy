package com.gawebersama.gawekuy.data.viewmodel

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

    companion object {
        const val TAG = "TransactionViewModel"
    }

    fun saveTransaction(
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
    ) {
        viewModelScope.launch {
            val result = transactionRepository.saveTransaction(
                orderId,
                serviceId.toString(),
                grossAmount,
                status,
                buyerId,
                buyerName,
                buyerEmail,
                sellerId,
                sellerName,
                sellerEmail,
                transactionTime
            )

            _operationResult.value = result
        }
    }
}