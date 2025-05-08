package com.example.foodapp.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodapp.Domain.BannerModel
import com.example.foodapp.Domain.CategoryModel
import com.example.foodapp.Domain.FoodModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class MainRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(CategoryModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return listData
    }

    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        val listData = MutableLiveData<MutableList<BannerModel>>()
        val ref = firebaseDatabase.getReference("Banner")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(BannerModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return listData
    }

    fun loadFiltered(id: String): LiveData<MutableList<FoodModel>> {
        val listData = MutableLiveData<MutableList<FoodModel>>()
        val ref = firebaseDatabase.getReference("Foods") // Sửa từ "Food" thành "Foods"
        val query: Query = ref.orderByChild("CategoryId").equalTo(id)
        query.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lists = mutableListOf<FoodModel>()
                    for (childSnapshot in snapshot.children) {
                        val list = childSnapshot.getValue(FoodModel::class.java)
                        if (list != null) {
                            lists.add(list)
                        }
                    }
                    Log.d("MainRepository", "Loaded ${lists.size} items for CategoryId: $id")
                    listData.value = lists
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainRepository", "Error loading filtered items: ${error.message}")
                }
            }
        )
        return listData
    }
}