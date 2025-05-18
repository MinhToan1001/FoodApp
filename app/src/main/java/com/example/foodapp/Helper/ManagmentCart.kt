package com.uilover.project2142.Helper

import android.content.Context
import android.widget.Toast
import com.example.foodapp.Domain.FoodModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uilover.project2142.Model.ChangeNumberItemsListener

class ManagmentCart(val context: Context) {
    private val database = FirebaseDatabase.getInstance().getReference("carts")
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val userId = sharedPreferences.getString("user_id", "") ?: ""

    // Lấy danh sách giỏ hàng từ Firebase
    fun getListCart(callback: (List<FoodModel>) -> Unit) {
        if (userId.isEmpty()) {
            Toast.makeText(context, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show()
            callback(emptyList())
            return
        }

        database.child(userId).child("items").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = mutableListOf<FoodModel>()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(FoodModel::class.java)
                    if (item != null) {
                        cartItems.add(item)
                    }
                }
                callback(cartItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi tải giỏ hàng: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(emptyList())
            }
        })
    }

    // Thêm món ăn vào giỏ hàng
    fun insertItem(item: FoodModel) {
        if (userId.isEmpty()) {
            Toast.makeText(context, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
            return
        }

        getListCart { currentCart ->
            val existingItem = currentCart.find { it.Title == item.Title }
            if (existingItem != null) {
                // Nếu món ăn đã có trong giỏ hàng, tăng số lượng
                existingItem.numberInCart += item.numberInCart
                database.child(userId).child("items").child(existingItem.Title).setValue(existingItem)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Lỗi cập nhật giỏ hàng: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Nếu món ăn chưa có, thêm mới vào giỏ hàng
                database.child(userId).child("items").child(item.Title).setValue(item)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Lỗi thêm vào giỏ hàng: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Giảm số lượng món ăn
    fun minusItem(listFood: ArrayList<FoodModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (userId.isEmpty()) return

        if (position < 0 || position >= listFood.size) return
        val currentCount = listFood[position].numberInCart
        if (currentCount <= 1) {
            // Nếu số lượng là 1, xóa món ăn khỏi giỏ hàng
            database.child(userId).child("items").child(listFood[position].Title).removeValue()
                .addOnSuccessListener {
                    listener.onChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            listFood[position].numberInCart = currentCount - 1
            database.child(userId).child("items").child(listFood[position].Title).setValue(listFood[position])
                .addOnSuccessListener {
                    listener.onChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Tăng số lượng món ăn
    fun plusItem(listFood: ArrayList<FoodModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (userId.isEmpty()) return

        listFood[position].numberInCart++
        database.child(userId).child("items").child(listFood[position].Title).setValue(listFood[position])
            .addOnSuccessListener {
                listener.onChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Tính tổng tiền
    fun getTotalFee(callback: (Double) -> Unit) {
        if (userId.isEmpty()) {
            callback(0.0)
            return
        }

        getListCart { cartItems ->
            val total = cartItems.sumOf { it.Price * it.numberInCart }
            callback(total)
        }
    }

    // Xóa toàn bộ giỏ hàng
    fun clearCart() {
        if (userId.isEmpty()) return
        database.child(userId).removeValue()
            .addOnFailureListener { e ->
                Toast.makeText(context, "Lỗi xóa giỏ hàng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}