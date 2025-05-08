package com.example.foodapp.Activity.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.foodapp.Activity.Dashboard.Banner
import com.example.foodapp.Activity.Dashboard.TopBar
import com.example.foodapp.Activity.admin.CategoryManage.CategoryManageScreen
import com.example.foodapp.Activity.admin.DashBoard.RevenueLineChart
import com.example.foodapp.ViewModel.MainViewModel
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun SetupNavGraph(navController: NavHostController) {
    val viewModel = MainViewModel()
    var selectedIndex by remember { mutableStateOf(0) } // Quản lý trạng thái BottomBar

    NavHost(
        navController = navController,
        startDestination = "admin_dashboard"
    ) {
        composable("admin_dashboard") {
            Scaffold(
                topBar = { TopBar() },
                bottomBar = {
                    AdminBottomBar(
                        selectedIndex = selectedIndex,
                        onItemClick = { index ->
                            selectedIndex = index
                            when (index) {
                                0 -> navController.navigate("admin_dashboard") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                1 -> navController.navigate("food_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                2 -> navController.navigate("order_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    item {
                        Banner(
                            banner = viewModel.banners.value ?: emptyList(),
                            showBannerLoading = viewModel.banners.value == null
                        )
                    }
                    item {
                        RevenueLineChart(year = 2025)
                    }
                }
            }
        }
        composable("food_management") {
            Scaffold(
                topBar = { TopBar() },
                bottomBar = {
                    AdminBottomBar(
                        selectedIndex = selectedIndex,
                        onItemClick = { index ->
                            selectedIndex = index
                            when (index) {
                                0 -> navController.navigate("admin_dashboard") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                1 -> navController.navigate("food_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                2 -> navController.navigate("order_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
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
                FoodManageScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    viewModel = viewModel
                )
            }
        }
        composable("order_management") {
            OrderManagementScreen(navController = navController, selectedIndex = selectedIndex) { index ->
                selectedIndex = index
            }
        }
        composable("category_management") {
            Scaffold(
                topBar = { TopBar() },
                bottomBar = {
                    AdminBottomBar(
                        selectedIndex = selectedIndex,
                        onItemClick = { index ->
                            selectedIndex = index
                            when (index) {
                                0 -> navController.navigate("admin_dashboard") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                1 -> navController.navigate("food_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                2 -> navController.navigate("order_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
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
                CategoryManageScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    viewModel = viewModel
                )
            }
        }
        composable("payment_management") {
            Scaffold(
                topBar = { TopBar() },
                bottomBar = {
                    AdminBottomBar(
                        selectedIndex = selectedIndex,
                        onItemClick = { index ->
                            selectedIndex = index
                            when (index) {
                                0 -> navController.navigate("admin_dashboard") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                1 -> navController.navigate("food_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                2 -> navController.navigate("order_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
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
                Text(
                    text = "Quản lý thanh toán (Chưa triển khai)",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        composable("settings") {
            Scaffold(
                topBar = { TopBar() },
                bottomBar = {
                    AdminBottomBar(
                        selectedIndex = selectedIndex,
                        onItemClick = { index ->
                            selectedIndex = index
                            when (index) {
                                0 -> navController.navigate("admin_dashboard") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                1 -> navController.navigate("food_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                2 -> navController.navigate("order_management") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
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
                Text(
                    text = "Cài đặt (Chưa triển khai)",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        composable("order_list/{status}") { backStackEntry ->
            val status = backStackEntry.arguments?.getString("status") ?: "Chưa xác nhận"
            OrderListByStatusScreen(
                status = status,
                navController = navController,
                selectedIndex = selectedIndex
            ) { index ->
                selectedIndex = index
            }
        }
    }
}