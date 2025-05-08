package com.example.foodapp.Domain

data class UserProfile(
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val fullName: String = "",
    val address: String = "",
    val password: String = "",
    val role: String = "user"
)