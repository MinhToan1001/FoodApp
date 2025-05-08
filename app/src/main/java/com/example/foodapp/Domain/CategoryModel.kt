package com.example.foodapp.Domain

import com.google.firebase.database.PropertyName

data class CategoryModel(
    @PropertyName("id") val id: Int = 0,
    @PropertyName("name") val name: String? = null,
    @PropertyName("imagePath") val imagePath: String? = null
) {
    constructor() : this(0, null, null) // Constructor rỗng cho Firebase
}