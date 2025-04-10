package com.gawebersama.gawekuy.data.repository

import com.google.firebase.firestore.FirebaseFirestore

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val transactionCollection = firestore.collection("transactions")

    companion object {
        const val TAG = "TransactionRepository"
    }

}