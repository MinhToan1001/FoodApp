package com.example.foodapp.Activity.admin.DashBoard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.Activity.Order.Order
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.util.*

data class MonthlyRevenue(val month: Int, val revenue: Float)

@Composable
fun RevenueLineChart(year: Int = 2025) {
    val database = FirebaseDatabase.getInstance().getReference("orders")
    var monthlyRevenue by remember { mutableStateOf(listOf<MonthlyRevenue>()) }

    // Lấy dữ liệu từ Firebase và nhóm theo tháng
    LaunchedEffect(year) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()
                for (data in snapshot.children) {
                    val order = data.getValue(Order::class.java)?.copy(orderId = data.key)
                    if (order != null && order.status == "Đã giao hàng") {
                        orders.add(order)
                    }
                }

                // Lấy năm từ timestamp và nhóm doanh thu theo tháng
                val calendar = Calendar.getInstance()
                val revenueMap = mutableMapOf<Int, Float>()
                for (order in orders) {
                    calendar.timeInMillis = order.timestamp
                    val orderYear = calendar.get(Calendar.YEAR)
                    if (orderYear == year) { // Chỉ lấy dữ liệu của năm được chọn
                        val month = calendar.get(Calendar.MONTH) + 1 // Tháng từ 1-12
                        revenueMap[month] = (revenueMap[month] ?: 0f) + order.totalAmount.toFloat()
                    }
                }

                // Tạo danh sách 12 tháng, nếu không có dữ liệu thì doanh thu = 0
                val revenueList = (1..12).map { month ->
                    MonthlyRevenue(month, (revenueMap[month] ?: 0f) / 10000f) // Chia cho 1000 để đổi sang Nghìn đồng
                }
                monthlyRevenue = revenueList
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
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

        if (monthlyRevenue.isEmpty()) {
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
                    .offset(x = (-40).dp), // Điều chỉnh để căn giữa trên trục Y
                textAlign = TextAlign.Center
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(start = 40.dp, end = 10.dp) // Giữ padding bất đối xứng
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val xStep = chartWidth / 11 // Chia trục X thành 12 phần (0-11)

                // Vẽ lưới ngang và nhãn trục Y
                val yStep = maxRevenue / 5 // Chia trục Y thành 5 phần
                for (i in 0..5) {
                    val y = chartHeight * (1 - i / 5f)
                    // Vẽ đường lưới ngang
                    drawLine(
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        color = Color.LightGray,
                        strokeWidth = 2f
                    )
                    // Vẽ nhãn trục Y
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
                        color = Color(0xFFFFA500), // Màu cam giống mẫu
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
    }
}