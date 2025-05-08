package com.example.foodapp.Activity.Order

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderScreen() {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getString("user_id", "") ?: ""
    val database = FirebaseDatabase.getInstance().getReference("orders")

    var orders by remember { mutableStateOf(listOf<Order>()) }
    var userFullName by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("fullName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userFullName = snapshot.getValue(String::class.java) ?: ""
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("OrderScreen", "Lỗi lấy họ tên: ${error.message}")
                    }
                })
        }
    }

    LaunchedEffect(Unit) {
        database.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orderList = mutableListOf<Order>()
                    for (data in snapshot.children) {
                        val order = data.getValue(Order::class.java)?.copy(orderId = data.key)
                        if (order != null) {
                            orderList.add(order)
                        }
                    }
                    orders = orderList.sortedByDescending { it.timestamp }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OrderScreen", "Lỗi lấy đơn hàng: ${error.message}")
                }
            })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Lịch sử đơn hàng",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        if (orders.isEmpty()) {
            item {
                Text(
                    text = "Không có đơn hàng nào",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            }
        } else {
            items(orders) { order ->
                OrderItem(order = order, userId = userId, userFullName = userFullName)
            }
        }
    }
}

@Composable
fun OrderItem(order: Order, userId: String, userFullName: String) {
    val decimalFormat = DecimalFormat("#,###")
    var showRatingDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<OrderItemData?>(null) }
    var userRatings by remember { mutableStateOf<Map<String, Rating>>(emptyMap()) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(order.orderId) {
        if (order.orderId == null || userId.isEmpty()) return@LaunchedEffect
        FirebaseDatabase.getInstance().getReference("orders")
            .child(order.orderId)
            .child("ratings")
            .child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ratingsMap = mutableMapOf<String, Rating>()
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val itemTitle = data.key ?: continue
                            if (data.exists() && data.hasChildren()) {
                                try {
                                    val ratingData = data.getValue(Rating::class.java)
                                    if (ratingData != null) {
                                        ratingsMap[itemTitle] = ratingData
                                    }
                                } catch (e: Exception) {
                                    Log.e("OrderItem", "Lỗi ánh xạ đánh giá tại $itemTitle: ${e.message}")
                                }
                            }
                        }
                    }
                    userRatings = ratingsMap
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OrderItem", "Lỗi lấy đánh giá: ${error.message}")
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Đơn hàng #${order.orderId?.takeLast(8) ?: "N/A"}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        order.items.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row {
                    Text(
                        text = "${item.title} (x${item.quantity})",
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${decimalFormat.format(item.price * item.quantity)} VNĐ",
                        fontSize = 16.sp
                    )
                }
                userRatings[item.title]?.let { rating ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Đánh giá của bạn cho ${item.title}:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            for (i in 1..rating.stars) {
                                Text(
                                    text = "★",
                                    color = Color.Yellow,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Text(
                            text = rating.comment,
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "Ngày đánh giá: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(rating.timestamp))}",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
        Text(
            text = "Tổng tiền: ${decimalFormat.format(order.totalAmount)} VNĐ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Trạng thái: ${order.status}",
            fontSize = 14.sp,
            color = when (order.status) {
                "Chưa xác nhận" -> Color.Red
                "Đã xác nhận" -> Color.Blue
                "Đang giao hàng" -> Color(0xFFFFA500)
                "Đã giao hàng" -> Color.Green
                else -> Color.Gray
            },
            modifier = Modifier.padding(top = 4.dp)
        )

        if (order.status == "Chưa xác nhận") {
            Button(
                onClick = {
                    if (order.orderId != null) {
                        FirebaseDatabase.getInstance().getReference("orders")
                            .child(order.orderId)
                            .child("status")
                            .setValue("Hủy đơn hàng")
                            .addOnSuccessListener {
                                Log.d("OrderItem", "Cập nhật trạng thái thành công: Hủy đơn hàng")
                            }
                            .addOnFailureListener { e ->
                                Log.e("OrderItem", "Lỗi cập nhật trạng thái: ${e.message}")
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color.Red, shape = RoundedCornerShape(8.dp))
                    .shadow(elevation = 0.dp, shape = RoundedCornerShape(8.dp)), // Bỏ bóng
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Hủy đơn hàng",
                    fontSize = 18.sp
                )
            }
        }

        if (order.status == "Đang giao hàng") {
            Button(
                onClick = {
                    if (order.orderId != null) {
                        FirebaseDatabase.getInstance().getReference("orders")
                            .child(order.orderId)
                            .child("status")
                            .setValue("Đã giao hàng")
                            .addOnSuccessListener {
                                Log.d("OrderItem", "Cập nhật trạng thái thành công: Đã giao hàng")
                            }
                            .addOnFailureListener { e ->
                                Log.e("OrderItem", "Lỗi cập nhật trạng thái: ${e.message}")
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color.Green, shape = RoundedCornerShape(8.dp))
                    .shadow(elevation = 0.dp, shape = RoundedCornerShape(8.dp)), // Bỏ bóng
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Đã nhận được hàng",
                    fontSize = 18.sp
                )
            }
        }

        if (order.status == "Đã giao hàng") {
            order.items.forEach { item ->
                if (userRatings[item.title] == null) {
                    Button(
                        onClick = {
                            selectedItem = item
                            showRatingDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(Color(0xFFFF6200), shape = RoundedCornerShape(8.dp))
                            .shadow(elevation = 0.dp, shape = RoundedCornerShape(8.dp)), // Bỏ bóng
                        contentPadding = PaddingValues(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6200), // Màu cam
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Đánh giá ${item.title}",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (showRatingDialog && selectedItem != null) {
            AlertDialog(
                onDismissRequest = {
                    showRatingDialog = false
                    selectedItem = null
                    rating = 0
                    comment = ""
                    errorMessage = ""
                },
                title = {
                    Text(
                        text = "Đánh giá sản phẩm: ${selectedItem!!.title}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Chọn số sao:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row {
                            for (i in 1..5) {
                                Text(
                                    text = "★",
                                    color = if (rating >= i) Color(0xFFFFD700) else Color.White,
                                    fontSize = 32.sp,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { rating = i }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bình luận:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        BasicTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.LightGray, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            textStyle = LocalTextStyle.current.copy(
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rating == 0) {
                                errorMessage = "Vui lòng chọn số sao!"
                                return@Button
                            }
                            if (order.orderId == null || selectedItem?.title == null) {
                                errorMessage = "Không thể lưu đánh giá: Dữ liệu không hợp lệ"
                                Log.e("OrderItem", "orderId=${order.orderId}, itemTitle=${selectedItem?.title}")
                                return@Button
                            }
                            if (userId.isEmpty() || userFullName.isEmpty()) {
                                errorMessage = "Không thể lưu đánh giá: Thông tin người dùng không hợp lệ"
                                Log.e("OrderItem", "userId=$userId, userFullName=$userFullName")
                                return@Button
                            }
                            if (comment.contains("[.#$\\[\\]]".toRegex())) {
                                errorMessage = "Bình luận chứa ký tự không hợp lệ (., #, $, [, ])"
                                return@Button
                            }

                            val newRating = Rating(
                                stars = rating,
                                comment = comment.trim(),
                                productTitle = selectedItem!!.title,
                                userId = userId,
                                fullName = userFullName,
                                timestamp = System.currentTimeMillis()
                            )
                            Log.d("OrderItem", "Chuẩn bị lưu đánh giá: $newRating")

                            // Lưu vào ratings
                            FirebaseDatabase.getInstance().getReference("ratings")
                                .push()
                                .setValue(newRating)
                                .addOnSuccessListener {
                                    Log.d("OrderItem", "Lưu đánh giá vào ratings thành công")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("OrderItem", "Lỗi lưu đánh giá vào ratings: ${e.message}", e)
                                    errorMessage = "Lỗi lưu đánh giá: Vui lòng thử lại"
                                    return@addOnFailureListener
                                }

                            // Lưu vào orders và cập nhật userRatings
                            FirebaseDatabase.getInstance().getReference("orders")
                                .child(order.orderId)
                                .child("ratings")
                                .child(userId)
                                .child(selectedItem!!.title)
                                .setValue(newRating)
                                .addOnSuccessListener {
                                    Log.d("OrderItem", "Lưu đánh giá vào orders thành công")
                                    // Cập nhật userRatings thủ công
                                    userRatings = userRatings.toMutableMap().apply {
                                        put(selectedItem!!.title, newRating)
                                    }
                                    // Đặt lại trạng thái
                                    showRatingDialog = false
                                    selectedItem = null
                                    rating = 0
                                    comment = ""
                                    errorMessage = ""
                                }
                                .addOnFailureListener { e ->
                                    Log.e("OrderItem", "Lỗi lưu đánh giá vào orders: ${e.message}", e)
                                    errorMessage = "Lỗi lưu đánh giá: Vui lòng thử lại"
                                }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color(0xFFFF6200), shape = RoundedCornerShape(8.dp))
                            .shadow(elevation = 0.dp, shape = RoundedCornerShape(8.dp)), // Bỏ bóng
                        contentPadding = PaddingValues(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6200), // Màu cam
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Gửi",
                            fontSize = 18.sp
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showRatingDialog = false
                            selectedItem = null
                            rating = 0
                            comment = ""
                            errorMessage = ""
                        },
                        modifier = Modifier
                            .background(Color(0xFFB0BEC5), shape = RoundedCornerShape(8.dp))
                            .shadow(elevation = 0.dp, shape = RoundedCornerShape(8.dp)), // Bỏ bóng
                        contentPadding = PaddingValues(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB0BEC5), // Màu bạc
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Hủy",
                            fontSize = 18.sp
                        )
                    }
                }
            )
        }
    }
}

data class Order(
    val orderId: String? = null,
    val userId: String = "",
    val items: List<OrderItemData> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "Chưa xác nhận",
    val timestamp: Long = 0L,
    val paymentMethod: String? = null
)

data class OrderItemData(
    val title: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)

data class Rating(
    val stars: Int = 0,
    val comment: String = "",
    val productTitle: String = "",
    val userId: String = "",
    val fullName: String = "",
    val timestamp: Long = 0L
)