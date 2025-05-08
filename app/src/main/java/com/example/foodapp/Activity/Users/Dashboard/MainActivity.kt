package com.example.foodapp.Activity.Dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodapp.Activity.Cart.CartScreen
import com.example.foodapp.Activity.Dashboard.Banner
import com.example.foodapp.Activity.Dashboard.CategorySection
import com.example.foodapp.Activity.Dashboard.MyBottomBar
import com.example.foodapp.Activity.Dashboard.Search
import com.example.foodapp.Activity.Dashboard.TopBar
import com.example.foodapp.Activity.Favorite.FavoriteScreen
import com.example.foodapp.Activity.Order.OrderScreen
import com.example.foodapp.Activity.Profile.EditProfileScreen
import com.example.foodapp.Activity.Profile.ProfileScreen
import com.example.foodapp.Activity.Splash.SplashMainActivity
import com.example.foodapp.Domain.BannerModel
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.ui.theme.FoodAppTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            FoodAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel = MainViewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val currentUserId by remember { mutableStateOf(sharedPreferences.getString("user_id", "") ?: "") }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = {
            MyBottomBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("main") {
                MainScreen(viewModel = viewModel)
            }
            composable("profile") {
                ProfileScreen(
                    navigateToEditProfile = { navController.navigate("edit_profile") },
                    navigateToOrderHistory = { navController.navigate("order_history") },
                    navigateToChangePassword = { navController.navigate("change_password") },
                    onLogout = {
                        sharedPreferences.edit().remove("user_id").apply()
                        val intent = Intent(context, SplashMainActivity::class.java)
                        intent.putExtra("from_logout", true)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                )
            }
            composable("edit_profile") {
                EditProfileScreen(
                    userId = currentUserId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("order_history") {
                Text("Màn hình Lịch sử Đơn hàng")
            }
            composable("change_password") {
                Text("Màn hình Đổi Mật Khẩu")
            }
            composable("login") {
                Text("Màn hình Đăng nhập")
            }
            composable("order") {
                OrderScreen()
            }
            composable("cart") {
                CartScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("favorite") {
                FavoriteScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = MainViewModel()) {
    val banners = remember { mutableStateOf<List<BannerModel>>(emptyList()) }
    val categories by viewModel.categories.observeAsState(initial = mutableListOf())
    val showBannerLoading = remember { mutableStateOf(true) }
    val showCategoryLoading = remember { mutableStateOf(true) }

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

    LaunchedEffect(categories) {
        showCategoryLoading.value = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Banner(
                banner = banners.value,
                showBannerLoading = showBannerLoading.value
            )
        }
        item {
            Search()
        }
        item {
            CategorySection(
                categories = categories,
                showCategoryLoading = showCategoryLoading.value
            )
        }
    }
}