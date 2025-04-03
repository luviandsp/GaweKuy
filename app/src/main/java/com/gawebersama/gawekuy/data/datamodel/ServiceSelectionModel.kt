package com.gawebersama.gawekuy.data.datamodel

import com.google.firebase.firestore.PropertyName

data class ServiceSelectionModel(
    @PropertyName("name") var name: String = "",
    @PropertyName("price") var price: Double = 0.0,
    @PropertyName("isSelected") var isSelected: Boolean = false
)
