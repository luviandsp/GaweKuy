package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class TransactionModel(
    @PropertyName("orderId") val orderId : String = "",
    @PropertyName("serviceId") val serviceId : String? = "",
    @PropertyName("serviceName") val serviceName : String? = "",
    @PropertyName("selectedServiceType") val selectedServiceType : String? = "",
    @PropertyName("grossAmount") val grossAmount : Int = 0,
    @PropertyName("statusForBuyer") val statusForBuyer : String = "",
    @PropertyName("statusForFreelancer") val statusForFreelancer : String = "",
    @PropertyName("buyerId") val buyerId : String = "",
    @PropertyName("buyerName") val buyerName : String = "",
    @PropertyName("buyerEmail") val buyerEmail : String = "",
    @PropertyName("buyerPhone") val buyerPhone : String = "",
    @PropertyName("sellerId") val sellerId : String = "",
    @PropertyName("sellerName") val sellerName : String = "",
    @PropertyName("sellerEmail") val sellerEmail : String = "",
    @PropertyName("sellerPhone") val sellerPhone : String = "",
    @PropertyName("transactionTime") val transactionTime : Timestamp = Timestamp.now()
)
