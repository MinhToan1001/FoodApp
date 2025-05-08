package com.example.foodapp.Activity.Profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.Domain.UserProfile
import com.google.firebase.database.FirebaseDatabase

@Composable
fun EditProfileScreen(
    userId: String, // Thêm tham số userId để xác định người dùng
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().getReference("users")

    // State cho các trường thông tin người dùng
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var currentUserProfile by remember { mutableStateOf<UserProfile?>(null) }

    // Tải thông tin người dùng hiện tại từ Firebase
    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            database.child(userId).get().addOnSuccessListener { snapshot ->
                val userProfile = snapshot.getValue(UserProfile::class.java)
                if (userProfile != null) {
                    currentUserProfile = userProfile
                    email = userProfile.email
                    phoneNumber = userProfile.phoneNumber
                    fullName = userProfile.fullName
                    address = userProfile.address
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Lỗi tải thông tin: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Chỉnh sửa thông tin cá nhân",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Họ và tên") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

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
                onClick = { onBack() },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Quay lại")
            }

            Button(
                onClick = {
                    // Kiểm tra định dạng email
                    if (!email.endsWith("@gmail.com")) {
                        Toast.makeText(context, "Email phải có định dạng @gmail.com", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (userId.isNotEmpty() && currentUserProfile != null) {
                        // Tạo đối tượng cập nhật chỉ với các trường đã thay đổi
                        val updates = mapOf(
                            "email" to email,
                            "phoneNumber" to phoneNumber,
                            "fullName" to fullName,
                            "address" to address
                        )
                        database.child(userId).updateChildren(updates)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Lỗi cập nhật: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Lưu")
            }
        }
    }
}