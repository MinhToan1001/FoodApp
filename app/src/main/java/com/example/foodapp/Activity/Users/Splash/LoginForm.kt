package com.example.foodapp.Activity.Splash

import android.content.Context
import android.content.Intent
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
import com.example.foodapp.Activity.admin.AdminActivity
import com.example.foodapp.Activity.Dashboard.MainActivity
import com.example.foodapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoginForm(
    onBack: () -> Unit = {},
    onForgotPassword: () -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val database: DatabaseReference = FirebaseDatabase.getInstance("https://foodapp-48431-default-rtdb.firebaseio.com/").getReference("users")
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding( top = 80.dp)
                .height(280.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "Đăng nhập",
            fontSize = 32.sp,
            fontFamily = PlayWriteFontFamily,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
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

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Quên mật khẩu?",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onForgotPassword() }
                .padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Vui lòng điền đầy đủ thông tin")
                    }
                    errorMessage = "Vui lòng nhập tên đăng nhập và mật khẩu"
                } else {
                    database.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                                val role = snapshot.child("role").getValue(String::class.java) ?: "user"

                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { authTask ->
                                        if (authTask.isSuccessful) {
                                            sharedPreferences.edit().putString("user_id", username).apply()
                                            errorMessage = null
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Đăng nhập thành công")
                                                if (role == "admin") {
                                                    context.startActivity(Intent(context, AdminActivity::class.java))
                                                } else {
                                                    context.startActivity(Intent(context, MainActivity::class.java))
                                                }
                                            }
                                        } else {
                                            errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng"
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Tên đăng nhập hoặc mật khẩu không đúng")
                                            }
                                        }
                                    }
                            } else {
                                errorMessage = "Tài khoản không tồn tại"
                                scope.launch {
                                    snackbarHostState.showSnackbar("Tài khoản không tồn tại")
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            errorMessage = "Lỗi: ${error.message}"
                            scope.launch {
                                snackbarHostState.showSnackbar("Lỗi: ${error.message}")
                            }
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange))
        ) {
            Text("Xác nhận", color = Color.White)
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