package com.gawebersama.gawekuy.data.datamodel

data class MidtransRequest(
    val orderId: String,
    val grossAmount: Int,
    val customerDetails: CustomerDetails,
    val itemDetails: List<ItemDetails>
)

data class CustomerDetails(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String
)

data class ItemDetails(
    val id: String,
    val price: Int,
    val quantity: Int,
    val name: String,
    val category: String
)