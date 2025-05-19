package com.example.foodapp.Activity.Dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodapp.Activity.Users.payment.VNPayUtils
import com.example.foodapp.Activity.Cart.CartScreen
import com.example.foodapp.Activity.Favorite.FavoriteScreen
import com.example.foodapp.Activity.Order.OrderScreen
import com.example.foodapp.Activity.Profile.ChangePasswordScreen
import com.example.foodapp.Activity.Profile.EditProfileScreen
import com.example.foodapp.Activity.Profile.ProfileScreen
import com.example.foodapp.Activity.Splash.SplashMainActivity
import com.example.foodapp.Domain.BannerModel
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.utils.SessionUtils
import com.example.foodapp.ui.theme.FoodAppTheme
import com.google.firebase.FirebaseApp
import com.uilover.project2142.Helper.ManagmentCart
import kotlinx.coroutines.CoroutineScope
import java.util.TreeMap

class MainActivity : ComponentActivity() {
    private var isDeepLinkHandled = false
    private var navController: NavController? = null // Lưu NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        handleDeepLink(intent)
        setContent {
            FoodAppTheme {
                AppNavigation(
                    onVNPayPayment = { amount ->
                        val userId = SessionUtils.getUserId(this)
                        VNPayUtils.processVNPayPayment(this, amount, userId) { err ->
                            Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                        }
                    },
                    snackbarHostState = SnackbarHostState(),
                    scope = rememberCoroutineScope(),
                    onNavControllerReady = { navController = it } // Lưu NavController
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with intent: $intent")
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        if (isDeepLinkHandled) {
            Log.d("DeepLink", "Deep link already handled, skipping")
            return
        }
        intent?.data?.toString()?.let { url ->
            Log.d("DeepLink", "Received URL: $url")
            if (url.startsWith("myfoodapp://payment/verify")) {
                isDeepLinkHandled = true
                handlePaymentResult(url)
            } else {
                Log.w("DeepLink", "Unknown deep link: $url")
                navigateToMainActivity()
            }
        } ?: Log.w("DeepLink", "No deep link data in intent")
    }

    private fun handlePaymentResult(result: String) {
        Log.d("PaymentResult", "Received result: $result")
        if (result.isEmpty() || !result.startsWith("myfoodapp://payment/verify")) {
            Log.e("PaymentResult", "Invalid or empty result URL: $result")
            Toast.makeText(this, "Lỗi xử lý thanh toán: URL không hợp lệ", Toast.LENGTH_LONG).show()
            navigateToMainActivity()
            return
        }

        val uri = Uri.parse(result)
        val vnp_SecureHash = uri.getQueryParameter("vnp_SecureHash")
        val txnRef = uri.getQueryParameter("vnp_TxnRef")
        val transactionStatus = uri.getQueryParameter("vnp_TransactionStatus")
        Log.d("PaymentResult", "txnRef: $txnRef, vnp_SecureHash: $vnp_SecureHash, transactionStatus: $transactionStatus")

        if (vnp_SecureHash == null || txnRef == null || transactionStatus == null) {
            Log.e("PaymentResult", "Missing required parameters in URL")
            Toast.makeText(this, "Lỗi xử lý thanh toán: Thiếu tham số", Toast.LENGTH_LONG).show()
            navigateToMainActivity()
            return
        }

        val params = TreeMap<String, String>()
        uri.queryParameterNames.forEach { key ->
            if (key != "vnp_SecureHash") {
                params[key] = uri.getQueryParameter(key) ?: ""
            }
        }
        val signData = params.entries.joinToString("&") { "${it.key}=${java.net.URLEncoder.encode(it.value, "UTF-8")}" }
        val computedHash = VNPayUtils.hmacSHA512("ZY654UJMW28I5NDNZMDFA7YO9RKNJT2B", signData)
        Log.d("PaymentResult", "Computed hash: $computedHash, Expected hash: $vnp_SecureHash")

        val userId = SessionUtils.getUserId(this)
        Log.d("PaymentResult", "UserId after payment: $userId")
        if (userId.isEmpty()) {
            Log.w("MainActivity", "User ID empty after payment, redirecting to SplashMainActivity")
            startActivity(Intent(this, SplashMainActivity::class.java))
            finish()
            return
        }

        if (computedHash == vnp_SecureHash) {
            if (transactionStatus == "00") {
                VNPayUtils.updateOrderStatusAfterPayment(this, txnRef, userId, {
                    Log.d("PaymentResult", "Payment successful, navigating to OrderScreen")
                    Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show()
                    navController?.navigate("order") {
                        popUpTo(navController!!.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    } ?: Log.e("PaymentResult", "NavController is null")
                }, { error ->
                    Log.e("PaymentResult", "Error updating order: $error")
                    Toast.makeText(this, "Lỗi cập nhật đơn hàng: $error", Toast.LENGTH_LONG).show()
                    navController?.navigate("cart") {
                        popUpTo(navController!!.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    } ?: Log.e("PaymentResult", "NavController is null")
                })
            } else {
                Log.d("PaymentResult", "Payment failed or canceled, status: $transactionStatus")
                Toast.makeText(this, "Thanh toán thất bại hoặc bị hủy", Toast.LENGTH_LONG).show()
                navController?.navigate("cart") {
                    popUpTo(navController!!.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                } ?: Log.e("PaymentResult", "NavController is null")
            }
        } else {
            Log.e("PaymentResult", "Invalid secure hash")
            Toast.makeText(this, "Kết quả thanh toán không hợp lệ", Toast.LENGTH_LONG).show()
            navController?.navigate("cart") {
                popUpTo(navController!!.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            } ?: Log.e("PaymentResult", "NavController is null")
        }
    }

    private fun navigateToMainActivity(cart: Boolean = false) {
        navController?.navigate("main") {
            popUpTo(navController!!.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        } ?: run {
            val intent = Intent(this, MainActivity::class.java)
            if (cart) {
                intent.putExtra("navigate_to_cart", true)
            }
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel = MainViewModel(),
    onVNPayPayment: (Double) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onNavControllerReady: (NavController) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val currentUserId by remember {
        mutableStateOf(SessionUtils.getUserId(context)).also {
            Log.d("AppNavigation", "Current userId: ${it.value}")
            if (it.value.isEmpty()) {
                Log.w("AppNavigation", "User ID empty, redirecting to SplashMainActivity")
                context.startActivity(Intent(context, SplashMainActivity::class.java))
                (context as? Activity)?.finish()
            }
        }
    }

    LaunchedEffect(navController) {
        onNavControllerReady(navController)
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { MyBottomBar(navController = navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("main") { MainScreen(viewModel = viewModel) }
            composable("profile") {
                ProfileScreen(
                    navigateToEditProfile = { navController.navigate("edit_profile") },
                    navigateToOrderHistory = { navController.navigate("order_history") },
                    navigateToChangePassword = { navController.navigate("change_password") },
                    onLogout = {
                        Log.d("ProfileScreen", "Logout triggered intentionally")
                        SessionUtils.clearUserId(context)
                        context.startActivity(Intent(context, SplashMainActivity::class.java))
                        (context as? Activity)?.finish()
                    }
                )
            }
            composable("edit_profile") {
                EditProfileScreen(userId = currentUserId, onBack = { navController.popBackStack() })
            }
            composable("order_history") { Text("Màn hình Lịch sử Đơn hàng") }
            composable("change_password") {
                ChangePasswordScreen(
                    userId = currentUserId,
                    onBack = { navController.popBackStack() },
                    snackbarHostState = snackbarHostState,
                    scope = scope
                )
            }
            composable("order") { OrderScreen() }
            composable("cart") {
                CartScreen(
                    navController = navController,
                    onVNPayPayment = onVNPayPayment
                )
            }
            composable("favorite") { FavoriteScreen() }
        }
    }
}
@Composable
fun MainScreen(viewModel: MainViewModel = MainViewModel()) {
    val banners = remember { mutableStateOf<List<BannerModel>>(emptyList()) }
    val categories by viewModel.categories.observeAsState(initial = emptyList())
    val showBannerLoading = remember { mutableStateOf(true) }
    val showCategoryLoading = remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter categories based on search query
    val filteredCategories = categories.filter { category ->
        category.name?.contains(searchQuery, ignoreCase = true) ?: false
    }

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
            Search(
                onSearchQueryChanged = { query ->
                    searchQuery = query
                    Log.d("MainScreen", "Search query: $query, Filtered categories: ${filteredCategories.size}")
                }
            )
        }
        item {
            CategorySection(
                categories = filteredCategories,
                showCategoryLoading = showCategoryLoading.value,
                searchQuery = searchQuery // Pass searchQuery
            )
        }
    }
}