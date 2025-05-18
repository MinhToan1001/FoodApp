package com.example.foodapp.Activity.Splash

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

    // Kiểm tra kết nối internet
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: run {
        errorMessage = "Vui lòng kiểm tra kết nối internet"
        return
    }
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: run {
        errorMessage = "Vui lòng kiểm tra kết nối internet"
        return
    }
    if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
        errorMessage = "Vui lòng kiểm tra kết nối internet"
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Vui lòng kiểm tra kết nối internet")
        }
        return
    }

    // Kiểm tra Google Play Services
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val result = googleApiAvailability.isGooglePlayServicesAvailable(context)
    if (result != com.google.android.gms.common.ConnectionResult.SUCCESS) {
        errorMessage = "Vui lòng cập nhật Google Play Services"
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Vui lòng cập nhật Google Play Services")
        }
        return
    }

    // Hiển thị Snackbar khi có thông báo
    LaunchedEffect(showSnackbarMessage) {
        showSnackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            showSnackbarMessage = null // Reset sau khi hiển thị
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.darkBrown))
            .padding(24.dp)
    ) {
        Text(
            text = "Quên Mật Khẩu",
            fontSize = 32.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
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
            onClick = {
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange)),
            enabled = !isEmailSent
        ) {
            Text(if (isEmailSent) "Đã gửi email" else "Gửi email đặt lại", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onBack() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text("Quay lại", color = Color.White)
        }
    }
}