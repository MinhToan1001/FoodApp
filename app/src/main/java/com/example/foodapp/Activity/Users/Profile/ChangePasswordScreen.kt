package com.example.foodapp.Activity.Profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(
    userId: String,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance("https://foodapp-48431-default-rtdb.firebaseio.com/").getReference("users")
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.white))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Đổi Mật Khẩu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it; errorMessage = null; successMessage = null },
            label = { Text("Mật khẩu hiện tại", color = Color.Black) },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )
        errorMessage?.let { message ->
            if (message == "Mật khẩu hiện tại không đúng") {
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; errorMessage = null; successMessage = null },
            label = { Text("Mật khẩu mới", color = Color.Black) },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null; successMessage = null },
            label = { Text("Xác nhận mật khẩu mới", color = Color.Black) },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )
        errorMessage?.let { message ->
            if (message == "Mật khẩu mới và xác nhận không khớp" || message == "Mật khẩu mới phải có ít nhất 6 ký tự" || message == "Vui lòng nhập đầy đủ thông tin") {
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
        successMessage?.let { message ->
            Text(
                text = message,
                color = Color.Green,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = "Vui lòng nhập đầy đủ thông tin"
                    successMessage = null
                    return@Button
                }

                if (newPassword != confirmPassword) {
                    errorMessage = "Mật khẩu mới và xác nhận không khớp"
                    successMessage = null
                    return@Button
                }

                if (newPassword.length < 6) {
                    errorMessage = "Mật khẩu mới phải có ít nhất 6 ký tự"
                    successMessage = null
                    return@Button
                }

                isLoading = true
                // Lấy email của người dùng từ Realtime Database
                database.child(userId).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val email = snapshot.child("email").getValue(String::class.java) ?: ""
                        if (email.isEmpty()) {
                            isLoading = false
                            errorMessage = "Không tìm thấy email của người dùng"
                            successMessage = null
                            return@addOnSuccessListener
                        }

                        // Xác thực lại người dùng với mật khẩu hiện tại
                        val credential = EmailAuthProvider.getCredential(email, currentPassword)
                        auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                // Đổi mật khẩu trên Firebase Authentication
                                auth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        isLoading = false
                                        errorMessage = null
                                        successMessage = "Đổi mật khẩu thành công"
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Đổi mật khẩu thành công")
                                            onBack()
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = "Lỗi: ${updateTask.exception?.message}"
                                        successMessage = null
                                    }
                                }
                            } else {
                                isLoading = false
                                errorMessage = "Mật khẩu hiện tại không đúng"
                                successMessage = null
                            }
                        }
                    } else {
                        isLoading = false
                        errorMessage = "Người dùng không tồn tại"
                        successMessage = null
                    }
                }.addOnFailureListener { error ->
                    isLoading = false
                    errorMessage = "Lỗi: ${error.message}"
                    successMessage = null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange)),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Xác nhận", color = Color.White, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Quay lại", color = Color.White, fontSize = 16.sp)
        }
    }
}