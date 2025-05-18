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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.foodapp.Activity.Dashboard.MainActivity
import com.example.foodapp.R
import com.example.foodapp.ui.theme.FoodAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

val PlayWriteFontFamily = FontFamily(
    Font(R.font.play_write)
)

class SplashMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    showLoginFormInitially = fromLogout
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
    showLoginFormInitially: Boolean = false
) {
    var showRegisterForm by remember { mutableStateOf(false) }
    var showLoginForm by remember { mutableStateOf(showLoginFormInitially) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.segaiha_black),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween, // Phân bố đều các thành phần
            horizontalAlignment = Alignment.CenterHorizontally // Canh giữa theo chiều ngang
        ) {
            if (!showRegisterForm && !showLoginForm) {
                // Logo và dòng chữ được đặt ở giữa khung hình
                Box(
                    modifier = Modifier
                        .weight(0.8f) // Chiếm không gian để căn giữa dọc
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center // Canh giữa nội dung trong Box
                ) {
                    ConstraintLayout {
                        val (logo, logoText) = createRefs()

                        Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(300.dp) // Điều chỉnh kích thước logo nếu cần
                                .constrainAs(logo) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        )

                        Text(
                            text = "Bờm Restaurant",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlayWriteFontFamily,
                            color = Color.White,
                            modifier = Modifier
                                .constrainAs(logoText) {
                                    top.linkTo(logo.bottom, margin = 8.dp)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        )
                    }
                }

                // Văn bản chào mừng và nút
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val styledText = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = colorResource(R.color.orange))) {
                            append(" ' Hương vị tinh tế, trải nghiệm đắm say '")
                        }
                    }
                    Text(
                        text = styledText,
                        fontSize = 16.7.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlayWriteFontFamily,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp)
                    )
                    GetStartedButton(
                        onRegisterClick = { showRegisterForm = true },
                        onLoginClick = { showLoginForm = true },
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .padding(bottom = 60.dp)
                    )
                }
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
                    scope = scope,
                    onForgotPassword = { showLoginForm = false }

                )
            }
        }
    }
}

@Composable
fun SplashScreenPreviewWrapper() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    SplashScreen(
        onGetStartedClick = {},
        snackbarHostState = snackbarHostState,
        scope = scope,
        modifier = Modifier,
        showLoginFormInitially = false
    )
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    FoodAppTheme {
        SplashScreenPreviewWrapper()
    }
}