package com.gawebersama.gawekuy.data.api

import com.gawebersama.gawekuy.data.datamodel.MidtransRequest
import com.gawebersama.gawekuy.data.datamodel.MidtransResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MidtransApiService {
    @POST("charge")
    suspend fun createTransaction(
        @Body request: MidtransRequest
    ): Response<MidtransResponse>
}
