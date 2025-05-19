package com.example.foodapp.Activity.Splash

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R

@Composable
@Preview
fun GetStartedButton(
    onRegisterClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Button(
            onClick = onRegisterClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .padding(end = 16.dp)
                .fillMaxWidth(0.35f)
                .border(1.dp, Color.White, shape = RoundedCornerShape(50.dp))
                .height(50.dp)
        ) {
            Text(
                text = "Đăng ký",
                fontSize = 16.sp,
                color = Color.White
            )
        }
        Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange)),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Đăng Nhập",
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}