package com.example.foodapp.Helper

import android.content.Context
import com.example.foodapp.Domain.FoodModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoriteManager(val context: Context) {
    private val database = FirebaseDatabase.getInstance().getReference("favorites")
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val userId = sharedPreferences.getString("user_id", "") ?: ""

    fun addFavorite(item: FoodModel) {
        if (userId.isEmpty()) return
        database.child(userId).child("items").child(item.Title).setValue(item)
    }

    fun removeFavorite(item: FoodModel) {
        if (userId.isEmpty()) return
        database.child(userId).child("items").child(item.Title).removeValue()
    }

    fun isFavorite(item: FoodModel, callback: (Boolean) -> Unit) {
        if (userId.isEmpty()) {
            callback(false)
            return
        }

        database.child(userId).child("items").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isFav = snapshot.children.any { it.getValue(FoodModel::class.java)?.Title == item.Title }
                callback(isFav)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    fun getFavorites(callback: (List<FoodModel>) -> Unit) {
        if (userId.isEmpty()) {
            callback(emptyList())
            return
        }

        database.child(userId).child("items").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favorites = mutableListOf<FoodModel>()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(FoodModel::class.java)
                    if (item != null) {
                        favorites.add(item)
                    }
                }
                callback(favorites)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}