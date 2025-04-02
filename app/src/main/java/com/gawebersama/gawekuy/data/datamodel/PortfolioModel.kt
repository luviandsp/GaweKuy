package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class PortfolioModel(
    @PropertyName("portfolioId") val portfolioId: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("portfolioTitle") val portfolioTitle: String = "",
    @PropertyName("portfolioDesc") val portfolioDesc: String? = null,
    @PropertyName("portfolioBannerImage") val portfolioBannerImage: String? = null,
)