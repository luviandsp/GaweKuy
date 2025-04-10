package com.gawebersama.gawekuy.data.api

import com.gawebersama.gawekuy.BuildConfig
import com.gawebersama.gawekuy.data.datamodel.MidtransRequest
import com.gawebersama.gawekuy.data.datamodel.MidtransResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MidtransApiService {
    @POST(BuildConfig.BACKEND_BASE_URL + "charge") // endpoint backend kamu
    suspend fun createTransaction(
        @Body request: MidtransRequest
    ): Response<MidtransResponse>
}
