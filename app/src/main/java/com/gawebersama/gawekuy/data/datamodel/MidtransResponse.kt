package com.gawebersama.gawekuy.data.datamodel

data class MidtransChargeResponse(
    val token: String,
    val redirect_url: String
)

data class MidtransStatusResponse(
    val transaction_status: String,
    val status_code: String,
    val fraud_status: String?,
    val payment_type: String?,
    val gross_amount: String?
)
