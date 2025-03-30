package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class ServiceModel(
    @PropertyName("serviceId") val serviceId: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("serviceName") val serviceName: String = "",
    @PropertyName("serviceDesc") val serviceDesc: String? = null,
    @PropertyName("imageBannerUrl") val imageBannerUrl: String? = null,
    @PropertyName("serviceRating") val serviceRating: Double = 0.0,
    @PropertyName("serviceTypes") val serviceTypes : List<ServiceSelectionModel> = emptyList(),
    @PropertyName("minPrice") val minPrice: Double = 0.0,
    @PropertyName("serviceCategory") val serviceCategory: String = "",
    @PropertyName("serviceTags") val serviceTags: List<String>? = null,
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
)
