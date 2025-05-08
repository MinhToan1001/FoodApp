package com.example.foodapp.Activity.Dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.foodapp.R

@Composable
@Preview
fun TopBar() {
    ConstraintLayout(
        modifier = Modifier
            .padding(top = 48.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        // Tạo các tham chiếu riêng biệt
        val settings = createRef()
        val name = createRef()
        val notification = createRef()

        Image(
            painter = painterResource(R.drawable.settings_icon),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(settings) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .clickable { }
        )

        Column(
            modifier = Modifier
                .constrainAs(name) {
                    top.linkTo(parent.top)
                    start.linkTo(settings.end)
                    end.linkTo(parent.end)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append("TTK")
                    }
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        append("FOOD")
                    }
                },
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = "Online Shop",
                color = Color.DarkGray,
                fontSize = 14.sp
            )
        }
        Image(painter = painterResource(R.drawable.bell_icon),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(notification) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }.clickable{}
        )
    }
}