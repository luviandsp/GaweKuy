package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class UserModel(
    @PropertyName("userId") var userId: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("phone") val phone: String = "",
    @PropertyName("role") val role: String = "",
    @PropertyName("profileImageUrl") val profileImageUrl: String? = null,
    @PropertyName("biography") val biography: String? = null,
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("userStatus") val userStatus: String? = null,
    @PropertyName("accountStatus") val accountStatus: Boolean = false,
    @PropertyName("paymentInfo") val paymentInfo: PaymentInfoModel? = null
)

