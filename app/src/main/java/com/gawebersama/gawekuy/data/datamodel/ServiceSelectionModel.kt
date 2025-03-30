package com.gawebersama.gawekuy.data.datamodel

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class ServiceSelectionModel(
    @PropertyName("name") var name: String = "",
    @PropertyName("price") var price: Double = 0.0,
    @PropertyName("isSelected") var isSelected: Boolean = false
)
