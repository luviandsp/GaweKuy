package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class PaymentInfoModel(
    @PropertyName("paymentType") val paymentType: String = "", // "bank" atau "ewallet"

    // Jika bank
    @PropertyName("bankName") val bankName: String? = null,
    @PropertyName("bankAccountName") val bankAccountName: String? = null,
    @PropertyName("bankAccountNumber") val bankAccountNumber: String? = null,

    // Jika e-wallet
    @PropertyName("ewalletType") val ewalletType: String? = null,
    @PropertyName("ewalletAccountName") val ewalletAccountName: String? = null,
    @PropertyName("ewalletNumber") val ewalletNumber: String? = null
)

