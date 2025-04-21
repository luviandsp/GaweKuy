package com.gawebersama.gawekuy.data.api

import com.gawebersama.gawekuy.data.datamodel.NotificationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CloudflareApi {
    @POST("send-notification") // endpoint Cloudflare Worker
    suspend fun sendNotification(
        @Body request: NotificationRequest
    ): Response<Unit>
}