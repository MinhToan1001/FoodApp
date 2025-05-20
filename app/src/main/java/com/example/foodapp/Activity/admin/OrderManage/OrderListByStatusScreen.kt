package com.example.foodapp.Activity.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodapp.Activity.Dashboard.TopBar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat

@Composable
fun OrderListByStatusScreen(
    status: String,
    navController: NavController,
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit
) {
    val database = FirebaseDatabase.getInstance().getReference("orders")
    var orders by remember { mutableStateOf(listOf<Order>()) }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
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

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val filteredOrders = orders.filter {
        when (status) {
            "Chưa xác nhận" -> {
                it.status == "Chưa xác nhận" ||
                        it.status == "Đang chờ xử lý" ||
                        it.status == "Chưa xác nhận (Đã thanh toán)"
            }
            else -> it.status == status
        }
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = {
            AdminBottomBar(
                selectedIndex = selectedIndex,
                onItemClick = { index ->
                    onIndexChange(index)
                    when (index) {
                        0 -> navController.navigate("admin_dashboard") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        1 -> navController.navigate("food_management") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        2 -> {}
                        3 -> navController.navigate("category_management") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        4 -> navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Danh sách đơn hàng: $status",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (filteredOrders.isEmpty()) {
                Text(
                    text = "Không có đơn hàng nào",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            } else {
                LazyColumn {
                    items(filteredOrders, key = { it.orderId ?: "" }) { order ->
                        AdminOrderItem(order = order)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderItem(order: Order) {
    val decimalFormat = DecimalFormat("#,###")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Đơn hàng #${order.orderId?.takeLast(8) ?: "Không có ID"}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        order.items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
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
        }
        Text(
            text = "Tổng tiền: ${decimalFormat.format(order.totalAmount)} VNĐ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        // Display order status above payment status
        Text(
            text = "Trạng thái: ${if (order.status == "Chưa xác nhận (Đã thanh toán)") "Chưa xác nhận" else order.status}",
            fontSize = 14.sp,
            color = when (order.status) {
                "Chưa xác nhận", "Chưa xác nhận (Đã thanh toán)" -> Color.Red
                "Đã xác nhận" -> Color.Blue
                "Đang giao hàng" -> Color(0xFFFFA500)
                "Đã giao hàng" -> Color.Green
                "Hủy đơn hàng" -> Color.Gray
                else -> Color.Gray
            },
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "Thanh toán: ${order.paymentStatus}",
            fontSize = 14.sp,
            color = when (order.paymentStatus) {
                "Đã thanh toán" -> Color.Green
                "Thanh toán khi nhận hàng" -> Color(0xFFFFA500)
                else -> Color.Red
            },
            modifier = Modifier.padding(top = 4.dp)
        )

        if (order.status != "Đã giao hàng" && order.status != "Hủy đơn hàng") {
            Button(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Cập nhật trạng thái")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val allStatusOptions = listOf(
                    "Chưa xác nhận",
                    "Đã xác nhận",
                    "Đang giao hàng",
                    "Hủy đơn hàng"
                ).filter { it != order.status }
                allStatusOptions.forEach { newStatus ->
                    DropdownMenuItem(
                        onClick = {
                            order.orderId?.let { id ->
                                FirebaseDatabase.getInstance().getReference("orders")
                                    .child(id)
                                    .child("status")
                                    .setValue(newStatus)
                            }
                            expanded = false
                        }
                    ) {
                        Text(newStatus)
                    }
                }
            }
        }
    }
}

data class Order(
    val orderId: String? = null,
    val userId: String = "",
    val items: List<OrderItemData> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "Chưa xác nhận",
    val paymentMethod: String? = null,
    val paymentStatus: String = "Chưa thanh toán",
    val timestamp: Long = 0L
)

data class OrderItemData(
    val title: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)