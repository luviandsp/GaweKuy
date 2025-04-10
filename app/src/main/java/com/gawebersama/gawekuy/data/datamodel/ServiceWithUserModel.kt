package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class ServiceWithUserModel(
    val service: ServiceModel = ServiceModel(),
    val user: UserModel = UserModel(),
    val portfolio : List<PortfolioModel> = emptyList()
)
