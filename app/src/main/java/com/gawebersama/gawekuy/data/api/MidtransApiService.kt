package com.gawebersama.gawekuy.data.api

import com.gawebersama.gawekuy.data.datamodel.MidtransChargeResponse
import com.gawebersama.gawekuy.data.datamodel.MidtransRequest
import com.gawebersama.gawekuy.data.datamodel.MidtransStatusResponse
import com.gawebersama.gawekuy.data.datamodel.OrderStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MidtransApiService {
    @POST("charge")
    suspend fun createTransaction(
        @Body request: MidtransRequest
    ): Response<MidtransChargeResponse>


    @POST("transaction-status")
    suspend fun getTransactionStatus(
        @Body request: OrderStatusRequest
    ): Response<MidtransStatusResponse>
}
