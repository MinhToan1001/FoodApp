package com.example.foodapp.Activity.DetailEachFood

import android.icu.text.DecimalFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.foodapp.R

@Composable
fun FooterSection(
    onAddToCartClick: () -> Unit,
    totalPrice: Double,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
            .height(75.dp)
            .fillMaxWidth()
            .background(color = colorResource(R.color.grey))
            .padding(horizontal = 16.dp)
    ) {
        val (orderBtn, price) = createRefs()

        Button(
            onClick = onAddToCartClick,
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.orange)
            ),
            modifier = Modifier
                .width(200.dp)
                .height(50.dp)
                .constrainAs(orderBtn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.cart),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text("Thêm", fontSize = 16.sp, color = Color.White)
        }

        Column(
            modifier = Modifier
                .width(140.dp)
                .padding(start = 15.dp)
                .height(50.dp)
                .constrainAs(price) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
        ) {
            Text("Tổng tiền:", fontSize = 18.sp, color = colorResource(R.color.darkPurple))
            val decimalFormat = DecimalFormat("#,###")
            Text(
                "${decimalFormat.format(totalPrice)}đ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.red),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FooterSectionPreview() {
    FooterSection(
        onAddToCartClick = {},
        totalPrice = 120000.0
    )
}
