package com.example.foodapp.utils

import com.cloudinary.Cloudinary

object CloudinaryConfig {
    private const val CLOUD_NAME = "toanvip"
    private const val API_KEY = "919634828858254"
    private const val API_SECRET = "f2NMKUYdIv3Q6XJhlhvzF5lf8g4"

    // Khởi tạo Cloudinary trực tiếp
    val cloudinary: Cloudinary = Cloudinary(mapOf(
        "cloud_name" to CLOUD_NAME,
        "api_key" to API_KEY,
        "api_secret" to API_SECRET
    ))
}