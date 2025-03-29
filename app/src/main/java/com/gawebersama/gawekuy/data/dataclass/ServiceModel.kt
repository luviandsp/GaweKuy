package com.gawebersama.gawekuy.data.dataclass

import com.google.firebase.Timestamp

data class ServiceModel(
    val serviceId: String = "",
    val userId: String? = "",
    val serviceName: String?,
    val serviceDesc: String?,
    val imageBannerUrl: String?,
    val serviceTypes : List<ServiceSelectionModel>?,
    val createdAt: Timestamp?
)
