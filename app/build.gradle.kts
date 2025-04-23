import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.gms.google.services)
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.gawebersama.gawekuy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gawebersama.gawekuy"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")

        if (localPropertiesFile.exists()) {
            FileInputStream(localPropertiesFile).use { properties.load(it) }
        }

        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("SUPABASE_ANON_KEY", "")}\"")
        buildConfigField("String", "SECRET", "\"${properties.getProperty("SECRET", "")}\"")
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "MIDTRANS_CLIENT_KEY_SANDBOX", "\"${properties.getProperty("MIDTRANS_CLIENT_KEY_SANDBOX", "")}\"")
        buildConfigField("String", "MIDTRANS_CLIENT_KEY_PRODUCTION", "\"${properties.getProperty("MIDTRANS_CLIENT_KEY_PRODUCTION", "")}\"")
        buildConfigField("String", "BACKEND_BASE_URL", "\"${properties.getProperty("BACKEND_BASE_URL", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.inappmessaging.display)
    implementation(libs.firebase.messaging)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.storage.kt)

    // Midtrans
//    implementation(libs.midtrans.uikit.sandbox)
    implementation(libs.midtrans.uikit)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)


    // Image Picker
    implementation(libs.drjacky.imagepicker)
    implementation(libs.ucrop)

    // Flexbox Layout
    implementation(libs.flexbox)

    // Glide
    implementation(libs.glide)

    // Spinner
    implementation(libs.amazingspinner)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Data Store
    implementation(libs.androidx.datastore.preferences)

    // Navigation
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)

    // Country Code Picker for Phone Number
    implementation (libs.ccp)

    // Ktor
    implementation(libs.ktor.client.okhttp)

    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}