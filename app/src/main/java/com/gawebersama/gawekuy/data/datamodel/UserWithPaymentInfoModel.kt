package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class UserWithPaymentInfoModel(
    @PropertyName("user") val user: UserModel = UserModel(),
    @PropertyName("paymentInfo") val paymentInfo: PaymentInfoModel? = null
)
