package com.example.foodapp.Activity.DetailEachFood

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R

@Composable
fun DescriptionSection(description: String){
    Column {
        Text(
            text = "Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.darkPurple),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = description,
            fontSize = 18.sp,
            color = colorResource(R.color.darkPurple),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}