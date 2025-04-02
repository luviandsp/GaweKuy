package com.gawebersama.gawekuy.data.datamodel

data class ServiceWithUserModel(
    val service: ServiceModel,
    val user: UserModel,
    val portfolio : List<PortfolioModel> = emptyList()
)
