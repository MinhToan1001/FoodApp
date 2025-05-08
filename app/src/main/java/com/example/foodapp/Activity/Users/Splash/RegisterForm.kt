package com.example.foodapp.Activity.Splash

import android.content.Context
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RegisterForm(
    onBack: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val database: DatabaseReference = FirebaseDatabase.getInstance("https://foodapp-48431-default-rtdb.firebaseio.com/").getReference("users")
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.darkBrown))
            .padding(24.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.toan),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "Đăng Ký",
            fontSize = 32.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        TextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            placeholder = { Text("Tên đăng nhập", color = Color.Black, fontSize = 16.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            placeholder = { Text("Mật khẩu", color = Color.Black, fontSize = 16.sp) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye_on
                        ),
                        contentDescription = "Toggle password visibility",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            placeholder = { Text("Nhập lại mật khẩu", color = Color.Black, fontSize = 16.sp) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (confirmPasswordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye_on
                        ),
                        contentDescription = "Toggle confirm password visibility",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

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
                when {
                    username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                        scope.launch {
                            snackbarHostState.showSnackbar("Vui lòng điền đầy đủ thông tin")
                        }
                        errorMessage = null
                    }
                    password != confirmPassword -> {
                        errorMessage = "Mật khẩu không khớp"
                        scope.launch {
                            snackbarHostState.showSnackbar("Mật khẩu không khớp")
                        }
                    }
                    else -> {
                        val user = hashMapOf(
                            "username" to username,
                            "password" to password,
                            "role" to "user",
                            "email" to "", // Thêm các trường mặc định
                            "phoneNumber" to "",
                            "fullName" to "",
                            "address" to ""
                        )

                        database.child(username).setValue(user)
                            .addOnSuccessListener {
                                // Lưu username vào SharedPreferences
                                sharedPreferences.edit().putString("user_id", username).apply()

                                errorMessage = null
                                scope.launch {
                                    snackbarHostState.showSnackbar("Đăng ký thành công")
                                    onRegisterSuccess()
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = "Đăng ký thất bại: ${e.message}"
                                scope.launch {
                                    snackbarHostState.showSnackbar("Đăng ký thất bại: ${e.message}")
                                }
                            }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange))
        ) {
            Text("Xác nhận", color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text("Quay lại", color = Color.White)
        }
    }
}