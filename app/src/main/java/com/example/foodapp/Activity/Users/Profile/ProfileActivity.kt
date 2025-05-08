package com.example.foodapp.Activity.Profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.Activity.Dashboard.Banner
import com.example.foodapp.Domain.BannerModel
import com.example.foodapp.ViewModel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel = MainViewModel(),
    navigateToEditProfile: () -> Unit,
    navigateToOrderHistory: () -> Unit,
    navigateToChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    val banners = remember { mutableStateOf<List<BannerModel>>(emptyList()) }
    val showBannerLoading = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        viewModel.getBanners(
            onSuccess = { bannerList ->
                banners.value = bannerList
                showBannerLoading.value = false
            },
            onFailure = {
                showBannerLoading.value = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Banner(
                banner = banners.value,
                showBannerLoading = showBannerLoading.value
            )
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Thông tin khách hàng",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            ProfileMenuItem(
                title = "Thông tin cá nhân",
                onClick = { navigateToEditProfile() }
            )
        }

        item {
            ProfileMenuItem(
                title = "Lịch sử giao hành",
                onClick = { navigateToOrderHistory() }
            )
        }

        item {
            ProfileMenuItem(
                title = "Đổi Mật Khẩu",
                onClick = { navigateToChangePassword() }
            )
        }

        item {
            ProfileMenuItem(
                title = "Đăng Xuất",
                onClick = {
                    // Xóa user_id khỏi SharedPreferences
                    sharedPreferences.edit().remove("user_id").apply()
                    onLogout()
                    Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                },
                textColor = Color.Red
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit,
    textColor: Color = Color.Black
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = textColor
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}