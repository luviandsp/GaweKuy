package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class TransactionDetailModel(
    val transaction : TransactionModel = TransactionModel(),
    val service : ServiceModel = ServiceModel(),
    val buyer : UserModel = UserModel(),
    val seller : UserModel = UserModel()
)