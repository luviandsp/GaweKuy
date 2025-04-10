package com.gawebersama.gawekuy.data.api

import com.gawebersama.gawekuy.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val instance: MidtransApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MidtransApiService::class.java)
    }
}