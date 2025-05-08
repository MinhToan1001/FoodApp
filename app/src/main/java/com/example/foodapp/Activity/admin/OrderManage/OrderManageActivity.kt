package com.example.foodapp.Activity.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodapp.Activity.Dashboard.TopBar

@Composable
fun OrderManagementScreen(
    navController: NavController,
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit
) {
    val statuses = listOf("Chưa xác nhận", "Đã xác nhận", "Đang giao hàng", "Đã giao hàng")

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
                        2 -> {} // Ở lại màn hình hiện tại
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
                text = "Quản lý đơn hàng",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            statuses.forEach { status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color.White, shape = MaterialTheme.shapes.medium)
                        .clickable { navController.navigate("order_list/$status") }
                        .padding(16.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = ">",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}