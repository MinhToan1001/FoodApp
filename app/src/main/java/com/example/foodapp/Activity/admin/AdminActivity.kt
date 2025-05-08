package com.example.foodapp.Activity.admin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.foodapp.Activity.Dashboard.Banner
import com.example.foodapp.Activity.Dashboard.TopBar
import com.example.foodapp.Activity.admin.DashBoard.RevenueLineChart
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.ui.theme.FoodAppTheme

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodAppTheme {
                val navController = rememberNavController()
                SetupNavGraph(navController)
            }
        }
        // Xử lý Deep Link nếu có
        intent.data?.let { uri ->
            Log.d("AdminActivity", "Received Deep Link: $uri")
        }
    }
}

@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: MainViewModel,
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit
) {
    val banners by viewModel.banners.observeAsState(emptyList())
    var isLoadingBanners by remember { mutableStateOf(true) }

    LaunchedEffect(banners) {
        isLoadingBanners = banners.isEmpty()
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = {
            AdminBottomBar(selectedIndex) { index ->
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Banner(banner = banners, showBannerLoading = isLoadingBanners)
            }
            item {
                RevenueLineChart(year = 2025)
            }
        }
    }
}