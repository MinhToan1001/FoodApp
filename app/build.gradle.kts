plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.foodapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.foodapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.database)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("io.coil-kt:coil-compose:2.2.2") // Thư viện tải ảnh cho Jetpack Compose
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0") // Indicator cho ViewPager
    implementation("com.google.accompanist:accompanist-pager:0.28.0") // Hỗ trợ ViewPager trong Compose
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7") // Hỗ trợ vòng đời trong ViewModel
    implementation("androidx.compose.runtime:runtime-livedata:1.7.5") // Hỗ trợ LiveData trong Compose
    implementation("com.github.bumptech.glide:glide:4.13.0") // Thư viện tải ảnh Glide
    implementation("com.google.code.gson:gson:2.10.1") // Thư viện JSON
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0") // ConstraintLayout cho Compose
    implementation("androidx.compose.foundation:foundation:1.7.6") // Các thành phần cơ bản của Compose
    implementation("androidx.compose.ui:ui-tooling:1.7.5") // Công cụ hỗ trợ UI trong Compose
    implementation("com.google.android.material:material:1.12.0") // Material Components của Google
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.cloudinary:cloudinary-android:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.1.0")
    implementation("com.paypal.sdk:paypal-android-sdk:2.16.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

}