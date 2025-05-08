package com.example.foodapp.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodapp.Domain.BannerModel
import com.example.foodapp.Domain.CategoryModel
import com.example.foodapp.Domain.FoodModel
import com.example.foodapp.Repository.MainRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainViewModel : ViewModel() {
    private val repository = MainRepository()
    private val bannerDatabase = FirebaseDatabase.getInstance("https://foodapp-48431-default-rtdb.firebaseio.com/").getReference("Banners")
    private val favoritesDatabase = FirebaseDatabase.getInstance("https://foodapp-48431-default-rtdb.firebaseio.com/").getReference("Favorites")
    private val categoryDatabase = FirebaseDatabase.getInstance("https://foodapp-48431-default-rtdb.firebaseio.com/").getReference("Category")
    private val foodDatabase = FirebaseDatabase.getInstance("https://foodapp-48431-default-rtdb.firebaseio.com/").getReference("Foods")

    private val _favorites = MutableLiveData<List<FoodModel>>(emptyList())
    val favorites: LiveData<List<FoodModel>> = _favorites

    private val _categories = MutableLiveData<MutableList<CategoryModel>>(mutableListOf())
    val categories: LiveData<MutableList<CategoryModel>> = _categories

    private val _foods = MutableLiveData<MutableList<FoodModel>>(mutableListOf())
    val foods: LiveData<MutableList<FoodModel>> = _foods

    private val _banners = MutableLiveData<List<BannerModel>>(emptyList())
    val banners: LiveData<List<BannerModel>> = _banners // Thêm LiveData cho banners

    init {
        loadFavorites()
        loadCategoriesRealtime()
        loadFoodsRealtime()
        loadBannersRealtime() // Tải banners realtime
    }

    fun getBanners(onSuccess: (List<BannerModel>) -> Unit, onFailure: (Exception) -> Unit) {
        bannerDatabase.get().addOnSuccessListener { snapshot ->
            val bannerList = mutableListOf<BannerModel>()
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val banner = child.getValue(BannerModel::class.java)
                    if (banner != null) {
                        bannerList.add(banner)
                    }
                }
            }
            _banners.value = bannerList // Lưu vào LiveData
            onSuccess(bannerList)
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }

    private fun loadBannersRealtime() {
        bannerDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bannerList = mutableListOf<BannerModel>()
                for (child in snapshot.children) {
                    val banner = child.getValue(BannerModel::class.java)
                    if (banner != null) {
                        bannerList.add(banner)
                    }
                }
                _banners.value = bannerList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainViewModel", "Lỗi khi tải banners: ${error.message}")
                _banners.value = emptyList()
            }
        })
    }

    private fun loadCategoriesRealtime() {
        Log.d("MainViewModel", "Bắt đầu tải danh mục từ Firebase")
        categoryDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MainViewModel", "Đã nhận snapshot: ${snapshot.childrenCount} mục")
                val categoryList = mutableListOf<CategoryModel>()
                for (child in snapshot.children) {
                    try {
                        val category = child.getValue(CategoryModel::class.java)
                        if (category != null && category.name != null && category.imagePath != null) {
                            categoryList.add(category)
                            Log.d("MainViewModel", "Đã thêm danh mục: ${category.name}, ID: ${category.id}")
                        } else {
                            Log.w("MainViewModel", "Bỏ qua danh mục không hợp lệ: ${child.key}, dữ liệu: $category")
                        }
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Lỗi khi phân tích danh mục ${child.key}: ${e.message}", e)
                    }
                }
                categoryList.sortBy { it.id }
                Log.d("MainViewModel", "Danh sách danh mục cuối cùng: ${categoryList.map { it.name }}")
                _categories.value = categoryList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainViewModel", "Lỗi khi tải danh mục: ${error.message}, mã lỗi: ${error.code}")
                _categories.value = mutableListOf()
            }
        })
    }

    private fun loadFoodsRealtime() {
        Log.d("MainViewModel", "Bắt đầu tải món ăn từ Firebase")
        foodDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MainViewModel", "Đã nhận snapshot món ăn: ${snapshot.childrenCount} mục")
                val foodList = mutableListOf<FoodModel>()
                for (child in snapshot.children) {
                    try {
                        val food = child.getValue(FoodModel::class.java)
                        if (food != null && food.Title.isNotEmpty()) {
                            foodList.add(food)
                            Log.d("MainViewModel", "Đã thêm món ăn: ${food.Title}, ID: ${food.Id}")
                        } else {
                            Log.w("MainViewModel", "Bỏ qua món ăn không hợp lệ: ${child.key}, dữ liệu: $food")
                        }
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Lỗi khi phân tích món ăn ${child.key}: ${e.message}", e)
                    }
                }
                foodList.sortBy { it.Id }
                Log.d("MainViewModel", "Danh sách món ăn cuối cùng: ${foodList.map { it.Title }}")
                _foods.value = foodList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainViewModel", "Lỗi khi tải món ăn: ${error.message}, mã lỗi: ${error.code}")
                _foods.value = mutableListOf()
            }
        })
    }

    fun loadFiltered(id: String): LiveData<MutableList<FoodModel>> {
        return repository.loadFiltered(id)
    }

    fun toggleFavorite(food: FoodModel) {
        val currentFavorites = _favorites.value?.toMutableList() ?: mutableListOf()
        if (currentFavorites.any { it.Id == food.Id }) {
            currentFavorites.removeAll { it.Id == food.Id }
            favoritesDatabase.child(food.Id.toString()).removeValue()
        } else {
            currentFavorites.add(food)
            favoritesDatabase.child(food.Id.toString()).setValue(food)
        }
        _favorites.value = currentFavorites
    }

    fun isFavorite(foodId: Int): Boolean {
        return _favorites.value?.any { it.Id == foodId } ?: false
    }

    private fun loadFavorites() {
        favoritesDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favoriteList = mutableListOf<FoodModel>()
                for (data in snapshot.children) {
                    val food = data.getValue(FoodModel::class.java)
                    food?.let { favoriteList.add(it) }
                }
                _favorites.value = favoriteList
            }

            override fun onCancelled(error: DatabaseError) {
                // Có thể thêm xử lý lỗi nếu cần
            }
        })
    }

    fun getFoodById(foodId: Int, onResult: (FoodModel?) -> Unit) {
        FirebaseDatabase.getInstance("https://foodapp-48431-default-rtdb.firebaseio.com/")
            .getReference("Foods")
            .child(foodId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val food = snapshot.getValue(FoodModel::class.java)
                    onResult(food)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(null)
                }
            })
    }
}