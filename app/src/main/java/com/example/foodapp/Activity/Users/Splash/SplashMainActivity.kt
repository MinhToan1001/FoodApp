package com.example.foodapp.Activity.Splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.foodapp.Activity.Dashboard.MainActivity
import com.example.foodapp.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class SplashMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kiểm tra xem có phải khởi động từ đăng xuất không
        val fromLogout = intent.getBooleanExtra("from_logout", false)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { padding ->
                SplashScreen(
                    onGetStartedClick = {
                        startActivity(Intent(this, MainActivity::class.java))
                    },
                    snackbarHostState = snackbarHostState,
                    scope = scope,
                    modifier = Modifier.padding(padding),
                    showLoginFormInitially = fromLogout // Truyền trạng thái đăng xuất
                )
            }
        }
    }
}

@Composable
fun SplashScreen(
    onGetStartedClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    showLoginFormInitially: Boolean = false // Thêm tham số để kiểm soát hiển thị LoginForm
) {
    var showRegisterForm by remember { mutableStateOf(false) }
    var showLoginForm by remember { mutableStateOf(showLoginFormInitially) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.darkBrown))
    ) {
        if (!showRegisterForm && !showLoginForm) {
            ConstraintLayout(modifier = Modifier.padding(top = 48.dp)) {
                val (backgroundImg, logiImg) = createRefs()
                Image(
                    painter = painterResource(id = R.drawable.intro_pic),
                    contentDescription = null,
                    modifier = Modifier
                        .constrainAs(backgroundImg) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        }
                        .fillMaxWidth()
                )
                Image(
                    painter = painterResource(R.drawable.pizza),
                    contentDescription = null,
                    modifier = Modifier.constrainAs(logiImg) {
                        top.linkTo(backgroundImg.top)
                        bottom.linkTo(backgroundImg.bottom)
                        start.linkTo(backgroundImg.start)
                        end.linkTo(backgroundImg.end)
                    },
                    contentScale = ContentScale.Fit
                )
            }
            val styledText = buildAnnotatedString {
                append("Chào mừng bạn đến với \n")
                withStyle(style = SpanStyle(color = colorResource(R.color.orange))) {
                    append("Ẩm thực TTK")
                }
                append(" Hương vị tinh tế, trải nghiệm đắm say")
            }
            Text(
                text = styledText,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .padding(horizontal = 16.dp)
            )
            GetStartedButton(
                onRegisterClick = { showRegisterForm = true },
                onLoginClick = { showLoginForm = true },
                modifier = Modifier.padding(top = 40.dp)
            )
        } else if (showRegisterForm) {
            RegisterForm(
                onBack = { showRegisterForm = false },
                onRegisterSuccess = {
                    showRegisterForm = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Đăng ký thành công, vui lòng đăng nhập")
                    }
                },
                snackbarHostState = snackbarHostState,
                scope = scope
            )
        } else if (showLoginForm) {
            LoginForm(
                onBack = { showLoginForm = false },
                snackbarHostState = snackbarHostState,
                scope = scope
            )
        }
    }
}