package com.example.foodapp.Helper

import android.content.Context
import com.example.foodapp.Domain.FoodModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoriteManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addFavorite(item: FoodModel) {
        val favorites = getFavorites().toMutableList()
        if (!favorites.any { it.Id == item.Id }) {
            item.isFavorite = true
            favorites.add(item)
            saveFavorites(favorites)
        }
    }

    fun removeFavorite(item: FoodModel) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll { it.Id == item.Id }
        item.isFavorite = false
        saveFavorites(favorites)
    }

    fun getFavorites(): List<FoodModel> {
        val json = sharedPreferences.getString("favorites", null)
        return if (json != null) {
            val type = object : TypeToken<List<FoodModel>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun isFavorite(item: FoodModel): Boolean {
        return getFavorites().any { it.Id == item.Id }
    }

    private fun saveFavorites(favorites: List<FoodModel>) {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(favorites)
        editor.putString("favorites", json)
        editor.apply()
    }
}