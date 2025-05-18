package com.example.foodapp.Activity.Dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodapp.Activity.Cart.CartScreen
import com.example.foodapp.Activity.Favorite.FavoriteScreen
import com.example.foodapp.Activity.Order.OrderScreen
import com.example.foodapp.Activity.Profile.ChangePasswordScreen
import com.example.foodapp.Activity.Profile.EditProfileScreen
import com.example.foodapp.Activity.Profile.ProfileScreen
import com.example.foodapp.Activity.Splash.SplashMainActivity
import com.example.foodapp.Domain.BannerModel
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.ui.theme.FoodAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.uilover.project2142.Helper.ManagmentCart
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.RoundingMode

class MainActivity : ComponentActivity() {
    private val vnp_TmnCode = "ABKIV171" // Mã Sandbox từ VNPay, thay bằng giá trị thực tế
    private val vnp_HashSecret = "ZY654UJMW28I5NDNZMDFA7YO9RKNJT2B" // Khóa bí mật từ VNPay, thay bằng giá trị thực tế
    private val vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html" // URL Sandbox
    private val vnp_ReturnUrl = "https://yourdomain.com/payment/verify/VNPAY" // URL trả về, thay bằng URL tạm nếu cần

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            FoodAppTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                AppNavigation(
                    viewModel = MainViewModel(),
                    onVNPayPayment = { amount ->
                        processVNPayPayment(amount)
                    },
                    snackbarHostState = snackbarHostState,
                    scope = scope
                )
            }
        }
    }

    private fun processVNPayPayment(amount: Double) {
        try {
            // Chuẩn hóa amount
            val cleanAmount = amount.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            val vnp_Amount = (cleanAmount * 100).toLong()

            val vnp_Version = "2.1.0"
            val vnp_Command = "pay"
            val vnp_OrderInfo = "Thanh toan don hang FoodApp"
            val vnp_IpAddr = "127.0.0.1"
            val vnp_CreateDate = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val vnp_ExpireDate = SimpleDateFormat("yyyyMMddHHmmss").format(Date(System.currentTimeMillis() + 15 * 60 * 1000))
            val vnp_TxnRef = System.currentTimeMillis().toString()

            val params = TreeMap<String, String>()
            params["vnp_Version"] = vnp_Version
            params["vnp_Command"] = vnp_Command
            params["vnp_TmnCode"] = vnp_TmnCode
            params["vnp_Amount"] = vnp_Amount.toString()
            params["vnp_CreateDate"] = vnp_CreateDate
            params["vnp_ExpireDate"] = vnp_ExpireDate
            params["vnp_CurrCode"] = "VND"
            params["vnp_IpAddr"] = vnp_IpAddr
            params["vnp_Locale"] = "vn"
            params["vnp_OrderInfo"] = vnp_OrderInfo
            params["vnp_OrderType"] = "250000"
            params["vnp_ReturnUrl"] = vnp_ReturnUrl
            params["vnp_TxnRef"] = vnp_TxnRef

            val signData = params.entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
            val vnp_SecureHash = hmacSHA512(vnp_HashSecret, signData)
            params["vnp_SecureHash"] = vnp_SecureHash

            val paymentUrl = vnp_Url + "?" + params.entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
            Log.d("VNPayDebug", "Payment URL: $paymentUrl")
            Log.d("VNPayDebug", "Sign Data: $signData")
            Log.d("VNPayDebug", "Secure Hash: $vnp_SecureHash")

            setContent {
                VNPayPaymentWebView(paymentUrl = paymentUrl, onPaymentResult = { result ->
                    handlePaymentResult(result)
                })
            }
        } catch (e: Exception) {
            Log.e("VNPayError", "Lỗi khởi tạo thanh toán: ${e.message}", e)
            Toast.makeText(this, "Lỗi khởi tạo thanh toán: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun hmacSHA512(key: String, data: String): String {
        val algorithm = "HmacSHA512"
        val mac = Mac.getInstance(algorithm)
        val secretKeySpec = SecretKeySpec(key.toByteArray(), algorithm)
        mac.init(secretKeySpec)
        val bytes = mac.doFinal(data.toByteArray())
        return bytes.joinToString("") { String.format("%02x", it) }
    }

    private fun handlePaymentResult(result: String) {
        if (result.contains("vnp_TransactionStatus=00")) {
            Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_LONG).show()
            updateOrderStatusAfterPayment()
        } else {
            Toast.makeText(this, "Thanh toán thất bại hoặc bị hủy", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateOrderStatusAfterPayment() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "") ?: ""
        if (userId.isNotEmpty()) {
            val managmentCart = ManagmentCart(this)
            val orderRef = FirebaseDatabase.getInstance().getReference("orders").push()
            managmentCart.getListCart { cartItems ->
                val order = hashMapOf(
                    "userId" to userId,
                    "items" to cartItems.map {
                        mapOf(
                            "title" to it.Title,
                            "price" to it.Price,
                            "quantity" to it.numberInCart
                        )
                    },
                    "totalAmount" to (cartItems.sumOf { it.Price * it.numberInCart } + 0.0 + 10.0),
                    "status" to "Chưa xác nhận (Đã thanh toán)",
                    "paymentMethod" to "VNPay",
                    "timestamp" to System.currentTimeMillis()
                )
                orderRef.setValue(order)
                    .addOnSuccessListener {
                        managmentCart.clearCart()
                        Log.i("OrderSuccess", "Order saved successfully")
                        Toast.makeText(this, "Đơn hàng đã được lưu", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("OrderError", "Error saving order: ${e.message}", e)
                        Toast.makeText(this, "Lỗi lưu đơn hàng: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Log.e("OrderError", "User ID is empty")
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel = MainViewModel(),
    onVNPayPayment: (Double) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
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
                ChangePasswordScreen(
                    userId = currentUserId,
                    onBack = { navController.popBackStack() },
                    snackbarHostState = snackbarHostState,
                    scope = scope
                )
            }
            composable("login") {
                Text("Màn hình Đăng nhập")
            }
            composable("order") {
                OrderScreen()
            }
            composable("cart") {
                CartScreen(
                    navController = navController,
                    onVNPayPayment = onVNPayPayment
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

@Composable
fun VNPayPaymentWebView(paymentUrl: String, onPaymentResult: (String) -> Unit) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        if (url.startsWith("https://yourdomain.com/payment/verify/VNPAY")) {
                            onPaymentResult(url)
                            return true
                        }
                        return false
                    }
                }
                loadUrl(paymentUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}