package com.example.foodapp.Activity.Splash

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEmailSent by remember { mutableStateOf(false) }
    var showSnackbarMessage by remember { mutableStateOf<String?>(null) }

    // ... Kiểm tra kết nối và Google Play Service như trước ...

    // Hiển thị UI
    ForgotPasswordScreenContent(
        email = email,
        onEmailChange = { email = it },
        errorMessage = errorMessage,
        isEmailSent = isEmailSent,
        onSendClick = {
            if (email.isEmpty()) {
                errorMessage = "Vui lòng nhập email"
                showSnackbarMessage = "Vui lòng nhập email"
            } else if (!email.endsWith("@gmail.com")) {
                errorMessage = "Email phải có định dạng @gmail.com"
                showSnackbarMessage = "Email phải có định dạng @gmail.com"
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isEmailSent = true
                            showSnackbarMessage = "Email đặt lại mật khẩu đã được gửi tới $email"
                            Log.d("ForgotPassword", "Email sent successfully")
                        } else {
                            errorMessage = "Lỗi: ${task.exception?.message}"
                            showSnackbarMessage = "Lỗi: ${task.exception?.message}"
                            Log.e("ForgotPassword", "Email send failed", task.exception)
                        }
                    }
            }
        },
        onBack = onBack
    )

    // Snackbar
    LaunchedEffect(showSnackbarMessage) {
        showSnackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            showSnackbarMessage = null
        }
    }
}

@Composable
fun ForgotPasswordScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    errorMessage: String?,
    isEmailSent: Boolean,
    onSendClick: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Ảnh nền
        Image(
            painter = painterResource(id = R.drawable.segaiha_black), // Thay bằng ảnh của bạn
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Lớp nội dung UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(top = 20.dp)
        ) {
            Text(
                text = "Quên mật khẩu",
                fontSize = 32.sp,
                color = Color.White,
                fontFamily = PlayWriteFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                placeholder = { Text("Email", color = Color.Black, fontSize = 16.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSendClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange)),
                enabled = !isEmailSent
            ) {
                Text(if (isEmailSent) "Đã gửi email" else "Gửi email đặt lại", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Quay lại", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    ForgotPasswordScreenContent(
        email = "test@gmail.com",
        onEmailChange = {},
        errorMessage = "Email không hợp lệ",
        isEmailSent = false,
        onSendClick = {},
        onBack = {}
    )
}