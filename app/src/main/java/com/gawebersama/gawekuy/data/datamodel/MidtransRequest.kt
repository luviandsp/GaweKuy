package com.gawebersama.gawekuy.data.datamodel

data class MidtransRequest(
    val orderId: String,
    val grossAmount: Double,
    val customerDetails: CustomerDetails
)
