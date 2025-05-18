package com.example.foodapp.Activity.Cart

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.Domain.FoodModel
import com.example.foodapp.R
import com.google.firebase.database.FirebaseDatabase
import com.uilover.project2142.Helper.ManagmentCart

@Composable
fun DeliveryInfoBox(
    cartItems: ArrayList<FoodModel>,
    totalAmount: Double,
    managmentCart: ManagmentCart,
    onOrderPlaced: () -> Unit,
    onVNPayPayment: (Double) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getString("user_id", "") ?: ""
    val database = FirebaseDatabase.getInstance().getReference("users")

    var showForm by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var initialEmail by remember { mutableStateOf("") }
    var initialPhoneNumber by remember { mutableStateOf("") }
    var initialAddress by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            database.child(userId).get().addOnSuccessListener { snapshot ->
                val userProfile = snapshot.getValue(UserProfile::class.java)
                if (userProfile != null) {
                    email = userProfile.email
                    phoneNumber = userProfile.phoneNumber
                    address = userProfile.address
                    initialEmail = userProfile.email
                    initialPhoneNumber = userProfile.phoneNumber
                    initialAddress = userProfile.address
                }
            }
        }
    }

    if (!showForm) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            // Phần địa chỉ
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                InfoItem(
                    title = "Địa chỉ",
                    content = if (address.isNotEmpty()) address else "Chưa có địa chỉ",
                    icon = painterResource(R.drawable.location),
                    onClick = { showForm = true }
                )
            }
            // Phần phương thức thanh toán
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Phương thức thanh toán",
                    color = colorResource(R.color.darkPurple),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == "COD",
                        onClick = {
                            selectedPaymentMethod = if (selectedPaymentMethod == "COD") null else "COD"
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorResource(R.color.orange),
                            unselectedColor = Color.Gray
                        )
                    )
                    Text(
                        text = "COD",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable {
                                selectedPaymentMethod = if (selectedPaymentMethod == "COD") null else "COD"
                            },
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedPaymentMethod == "Payment",
                        onClick = { selectedPaymentMethod = "Payment" }
                    )
                    Text(
                        text = "Payment (VNPay)",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { selectedPaymentMethod = "Payment" },
                        fontSize = 16.sp
                    )
                }
            }
            // Phần nút đặt hàng
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Button(
                    onClick = {
                        when {
                            email.isEmpty() -> {
                                errorMessage = "Vui lòng nhập email, nhấn vào phần địa chỉ để cập nhật"
                                showForm = true
                            }
                            phoneNumber.isEmpty() -> {
                                errorMessage = "Vui lòng nhập số điện thoại, nhấn vào phần địa chỉ để cập nhật"
                                showForm = true
                            }
                            address.isEmpty() -> {
                                errorMessage = "Vui lòng nhập địa chỉ, nhấn vào phần địa chỉ để cập nhật"
                                showForm = true
                            }
                            selectedPaymentMethod == null -> errorMessage = "Vui lòng chọn phương thức thanh toán"
                            else -> {
                                val updates = mapOf(
                                    "email" to email,
                                    "phoneNumber" to phoneNumber,
                                    "address" to address
                                )
                                database.child(userId).updateChildren(updates)

                                if (selectedPaymentMethod == "Payment") {
                                    onVNPayPayment(totalAmount)
                                } else {
                                    val orderRef = FirebaseDatabase.getInstance().getReference("orders").push()
                                    val order = hashMapOf(
                                        "userId" to userId,
                                        "items" to cartItems.map {
                                            mapOf(
                                                "title" to it.Title,
                                                "price" to it.Price,
                                                "quantity" to it.numberInCart
                                            )
                                        },
                                        "totalAmount" to totalAmount,
                                        "status" to "Chưa xác nhận (COD)",
                                        "paymentMethod" to "COD",
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    orderRef.setValue(order).addOnSuccessListener {
                                        managmentCart.clearCart()
                                        onOrderPlaced()
                                    }.addOnFailureListener { exception ->
                                        errorMessage = "Lỗi khi đặt hàng: ${exception.message}"
                                    }
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.orange)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Đặt hàng",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(color = colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Số điện thoại") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Địa chỉ nhận hàng") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        email = initialEmail
                        phoneNumber = initialPhoneNumber
                        address = initialAddress
                        showForm = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("Hủy", color = Color.Black)
                }
                Button(
                    onClick = {
                        if (email.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
                            errorMessage = "Vui lòng nhập đầy đủ thông tin"
                            return@Button
                        }
                        val updates = mapOf(
                            "email" to email,
                            "phoneNumber" to phoneNumber,
                            "address" to address
                        )
                        database.child(userId).updateChildren(updates)
                        initialEmail = email
                        initialPhoneNumber = phoneNumber
                        initialAddress = address
                        showForm = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange))
                ) {
                    Text("Xác nhận", color = Color.White)
                }
            }
        }
    }

    errorMessage?.let {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Lỗi") },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun InfoItem(title: String, content: String, icon: androidx.compose.ui.graphics.painter.Painter, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = content,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class UserProfile(
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val fullName: String = "",
    val address: String = "",
    val password: String = "",
    val role: String = ""
)