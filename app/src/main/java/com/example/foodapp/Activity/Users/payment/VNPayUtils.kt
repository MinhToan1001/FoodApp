package com.example.foodapp.Activity.Users.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.example.foodapp.Activity.Splash.SplashMainActivity
import com.example.foodapp.utils.SessionUtils
import com.google.firebase.database.FirebaseDatabase
import com.uilover.project2142.Helper.ManagmentCart
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.math.RoundingMode

object VNPayUtils {
    private const val vnp_TmnCode = "ABKIV171"
    private const val vnp_HashSecret = "ZY654UJMW28I5NDNZMDFA7YO9RKNJT2B"
    private const val vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
    private const val vnp_ReturnUrl = "myfoodapp://payment/verify"

    fun processVNPayPayment(context: Context, amount: Double, userId: String, onError: (String) -> Unit) {
        if (userId.isEmpty()) {
            Log.w("VNPayUtils", "User ID empty, cannot process payment")
            onError("Vui lòng đăng nhập để thanh toán")
            context.startActivity(Intent(context, SplashMainActivity::class.java))
            (context as? Activity)?.finish()
            return
        }

        try {
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

            // Save order temporarily
            val managmentCart = ManagmentCart(context)
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
                    "status" to "Đang xử lý thanh toán",
                    "paymentMethod" to "VNPay",
                    "txnRef" to vnp_TxnRef,
                    "timestamp" to System.currentTimeMillis()
                )
                orderRef.setValue(order).addOnFailureListener { e ->
                    Log.e("VNPayError", "Error saving order: ${e.message}")
                    onError("Lỗi lưu đơn hàng: ${e.message}")
                }
            }

            val signData = params.entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
            val vnp_SecureHash = hmacSHA512(vnp_HashSecret, signData)
            params["vnp_SecureHash"] = vnp_SecureHash

            val paymentUrl = vnp_Url + "?" + params.entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
            Log.d("VNPayDebug", "Payment URL: $paymentUrl")

            // Open Chrome Custom Tabs
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, android.net.Uri.parse(paymentUrl))
        } catch (e: Exception) {
            Log.e("VNPayError", "Lỗi khởi tạo thanh toán: ${e.message}", e)
            onError("Lỗi khởi tạo thanh toán: ${e.message}")
        }
    }

    fun updateOrderStatusAfterPayment(context: Context, txnRef: String, userId: String, onComplete: () -> Unit, onError: (String) -> Unit) {
        if (userId.isEmpty()) {
            Log.w("VNPayUtils", "User ID empty, cannot update order")
            onError("Vui lòng đăng nhập để tiếp tục")
            context.startActivity(Intent(context, SplashMainActivity::class.java))
            (context as? Activity)?.finish()
            return
        }

        FirebaseDatabase.getInstance().getReference("orders")
            .orderByChild("txnRef").equalTo(txnRef)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            data.ref.child("status").setValue("Chưa xác nhận (Đã thanh toán)")
                                .addOnSuccessListener {
                                    val managmentCart = ManagmentCart(context)
                                    managmentCart.clearCart()
                                    Log.i("OrderSuccess", "Order updated successfully")
                                    onComplete()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("OrderError", "Error updating order: ${e.message}")
                                    onError("Lỗi cập nhật đơn hàng: ${e.message}")
                                }
                        }
                    } else {
                        Log.e("OrderError", "Order not found for txnRef: $txnRef")
                        onError("Không tìm thấy đơn hàng")
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e("OrderError", "Error querying order: ${error.message}")
                    onError("Lỗi truy vấn đơn hàng: ${error.message}")
                }
            })
    }

    fun hmacSHA512(key: String, data: String): String {
        val algorithm = "HmacSHA512"
        val mac = Mac.getInstance(algorithm)
        val secretKeySpec = SecretKeySpec(key.toByteArray(), algorithm)
        mac.init(secretKeySpec)
        val bytes = mac.doFinal(data.toByteArray())
        return bytes.joinToString("") { String.format("%02x", it) }
    }
}