package com.example.foodapp.Activity.Cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodapp.R
import java.text.DecimalFormat

@Composable
fun CartSummary(itemTotal: Double, tax: Double, delivery: Double) {
    val total = itemTotal + tax + delivery
    val decimalFormat = DecimalFormat("#,###")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(colorResource(R.color.grey))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Tổng tiền:",
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "${decimalFormat.format(itemTotal)} VNĐ")
        }
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Phí vận chuyển:",
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "${decimalFormat.format(delivery)} VNĐ")
        }
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Thuế:",
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "${decimalFormat.format(tax)} VNĐ")
        }
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .height(1.dp)
                .fillMaxWidth()
                .background(Color.Gray)
        )
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Số tiền thanh toán:",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.darkPurple)
            )
            Text(
                text = "${decimalFormat.format(total)} VNĐ",
                fontWeight = FontWeight.Bold
            )
        }
    }
}