package com.example.foodapp.Activity.Dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.foodapp.R

@Composable
fun Search(
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onSearchQueryChanged(newText)
        },
        label = {
            Text(
                text = "Bạn muốn ăn gì hôm nay?",
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    text = ""
                    onSearchQueryChanged("")
                }) {
                    Icon(
                        painter = painterResource(R.drawable.xacnhan), // Ensure ic_clear exists
                        contentDescription = "Clear search",
                        modifier = Modifier.size(22.dp),
                        tint = Color.DarkGray
                    )
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorResource(R.color.grey),
            unfocusedContainerColor = colorResource(R.color.grey),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color.DarkGray,
            unfocusedTextColor = Color.DarkGray,
            focusedPlaceholderColor = Color.Transparent,
            unfocusedPlaceholderColor = Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp)
            .background(colorResource(R.color.grey), CircleShape)
    )
}

@Preview
@Composable
fun SearchPreview() {
    Search(onSearchQueryChanged = {})
}