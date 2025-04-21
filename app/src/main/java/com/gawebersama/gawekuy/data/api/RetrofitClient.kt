package com.gawebersama.gawekuy.data.api

import com.gawebersama.gawekuy.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = BuildConfig.BACKEND_BASE_URL

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val noificationApi: CloudflareApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudflareApi::class.java)
    }

    val apiService: MidtransApiService by lazy {
        retrofit.create(MidtransApiService::class.java)
    }
}