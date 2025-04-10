package com.gawebersama.gawekuy.ui.main

import android.app.Application
import com.gawebersama.gawekuy.BuildConfig
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme
import com.midtrans.sdk.uikit.SdkUIFlowBuilder

class GawekuyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        SdkUIFlowBuilder.init()
            .setContext(this)
            .setClientKey(BuildConfig.MIDTRANS_CLIENT_KEY) // Ganti dengan Client Key Midtrans kamu
            .setMerchantBaseUrl(BuildConfig.BACKEND_BASE_URL) // URL endpoint backend kamu
            .enableLog(true)
            .setColorTheme(CustomColorTheme("#0084FF", "#004c94", "#0084FF"))
            .setLanguage("id")
            .buildSDK()
    }
}