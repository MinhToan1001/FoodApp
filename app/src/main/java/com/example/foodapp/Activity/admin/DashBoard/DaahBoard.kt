package com.example.foodapp.Activity.admin.DashBoard

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.Activity.Order.Order
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.util.*

data class MonthlyRevenue(val month: Int, val revenue: Float)

data class TopProduct(
    val title: String,
    val quantitySold: Int,
    val imageUrl: String // Đường dẫn hình ảnh của sản phẩm
)

@Composable
fun RevenueLineChart(year: Int = 2025) {
    val database = FirebaseDatabase.getInstance().getReference("orders")
    var monthlyRevenue by remember { mutableStateOf(listOf<MonthlyRevenue>()) }
    var topProducts by remember { mutableStateOf(listOf<TopProduct>()) }

    // Lấy dữ liệu từ Firebase
    LaunchedEffect(year) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()
                for (data in snapshot.children) {
                    val order = data.getValue(Order::class.java)?.copy(orderId = data.key)
                    if (order != null && (order.status == "Đã thanh toán" || order.status == "Đã giao hàng")) {
                        orders.add(order)
                    }
                }

                // Tính toán doanh thu theo tháng
                val calendar = Calendar.getInstance()
                val revenueMap = mutableMapOf<Int, Float>()
                val productSales = mutableMapOf<String, Pair<Int, String>>() // Map<Title, Pair<QuantitySold, ImageUrl>>

                for (order in orders) {
                    calendar.timeInMillis = order.timestamp
                    val orderYear = calendar.get(Calendar.YEAR)
                    if (orderYear == year) {
                        // Tính doanh thu theo tháng
                        val month = calendar.get(Calendar.MONTH) + 1 // Tháng từ 1-12
                        revenueMap[month] = (revenueMap[month] ?: 0f) + order.totalAmount.toFloat()

                        // Thống kê sản phẩm bán chạy
                        order.items.forEach { item ->
                            val current = productSales[item.title] ?: Pair(0, item.imageUrl)
                            productSales[item.title] = Pair(current.first + item.quantity, item.imageUrl)
                        }
                    }
                }

                // Tạo danh sách doanh thu theo tháng
                val revenueList = (1..12).map { month ->
                    MonthlyRevenue(month, (revenueMap[month] ?: 0f) / 10000f) // Chia cho 10000 để đổi sang Chục nghìn đồng
                }
                monthlyRevenue = revenueList

                // Lấy 4 sản phẩm bán chạy nhất
                topProducts = productSales.entries
                    .map { TopProduct(it.key, it.value.first, it.value.second) }
                    .sortedByDescending { it.quantitySold }
                    .take(4)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RevenueLineChart", "Error fetching orders: ${error.message}")
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tiêu đề chính
        Text(
            text = "Doanh thu năm $year",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        if (monthlyRevenue.isEmpty() || monthlyRevenue.all { it.revenue == 0f }) {
            Text(
                text = "Không có dữ liệu doanh thu",
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val maxRevenue = monthlyRevenue.maxOf { it.revenue } * 1.2f // Tăng 20% để có không gian trên cùng
            val decimalFormat = DecimalFormat("#,###")

            // Nhãn trục Y (đặt trên đỉnh trục Y)
            Text(
                text = "Doanh thu (Chục nghìn đồng)",
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .offset(x = (-40).dp),
                textAlign = TextAlign.Center
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(start = 40.dp, end = 10.dp)
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val xStep = chartWidth / 11

                // Vẽ lưới ngang và nhãn trục Y
                val yStep = maxRevenue / 5
                for (i in 0..5) {
                    val y = chartHeight * (1 - i / 5f)
                    drawLine(
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        color = Color.LightGray,
                        strokeWidth = 2f
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "${decimalFormat.format(yStep * i)}",
                        -10f,
                        y + 5f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }

                // Vẽ lưới dọc
                for (i in 0..11) {
                    val x = i * xStep
                    drawLine(
                        start = Offset(x, 0f),
                        end = Offset(x, chartHeight),
                        color = Color.LightGray,
                        strokeWidth = 2f
                    )
                }

                // Vẽ trục X và Y
                drawLine(
                    start = Offset(0f, 0f),
                    end = Offset(0f, chartHeight),
                    color = Color.Black,
                    strokeWidth = 4f
                )
                drawLine(
                    start = Offset(0f, chartHeight),
                    end = Offset(chartWidth, chartHeight),
                    color = Color.Black,
                    strokeWidth = 4f
                )

                // Vẽ đường doanh thu
                val path = Path()
                val points = mutableListOf<Offset>()
                monthlyRevenue.forEachIndexed { index, data ->
                    val x = index * xStep
                    val y = chartHeight * (1 - data.revenue / maxRevenue)
                    points.add(Offset(x, y))

                    // Vẽ nhãn tháng trên trục X
                    drawContext.canvas.nativeCanvas.drawText(
                        "T${data.month}",
                        x,
                        chartHeight + 40f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )

                    // Vẽ giá trị doanh thu tại mỗi điểm
                    if (data.revenue > 0) {
                        drawContext.canvas.nativeCanvas.drawText(
                            decimalFormat.format(data.revenue),
                            x,
                            y - 20f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 30f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }

                // Vẽ đường nối các điểm
                if (points.isNotEmpty()) {
                    path.moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x, points[i].y)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFFFA500),
                        style = Stroke(width = 5f)
                    )

                    // Vẽ các điểm trên đường
                    points.forEach { point ->
                        drawCircle(
                            color = Color(0xFFFFA500),
                            radius = 8f,
                            center = point
                        )
                    }
                }
            }

            // Nhãn trục X
            Text(
                text = "Tháng",
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 13.dp)
            )
        }

        // Phần sản phẩm bán chạy nhất
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Sản phẩm bán chạy nhất năm $year",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (topProducts.isEmpty()) {
            Text(
                text = "Không có sản phẩm nào được bán",
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(topProducts) { product ->
                    TopProductItem(product)
                }
            }
        }
    }
}

@Composable
fun TopProductItem(product: TopProduct) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = product.imageUrl.ifEmpty { "https://via.placeholder.com/80" },
            contentDescription = "Hình ảnh sản phẩm ${product.title}",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Đã bán: ${product.quantitySold}",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}