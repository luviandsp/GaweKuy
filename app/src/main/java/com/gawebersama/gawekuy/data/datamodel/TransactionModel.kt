package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class TransactionModel(
    @PropertyName("orderId") val orderId : String,
    @PropertyName("grossAmount") val grossAmount : Double,
    @PropertyName("status") val status : String,
    @PropertyName("buyer") val buyer : String,
    @PropertyName("buyerEmail") val buyerEmail : String,
    @PropertyName("sellerId") val sellerId : String,
    @PropertyName("sellerName") val sellerName : String,
)
