package com.example.foodapp.Activity.Favorite

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.foodapp.Domain.FoodModel
import com.example.foodapp.Helper.FavoriteManager
import com.example.foodapp.R

@Composable
fun FavoriteScreen() {
    val context = LocalContext.current
    val favoriteManager = remember { FavoriteManager(context) }
    var favorites by remember { mutableStateOf(favoriteManager.getFavorites()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Danh sách yêu thích",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.darkPurple)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (favorites.isEmpty()) {
            Text(
                text = "Chưa có sản phẩm yêu thích nào",
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = colorResource(R.color.darkPurple)
            )
        } else {
            LazyColumn {
                items(favorites) { item ->
                    FavoriteItem(
                        item = item,
                        onRemove = {
                            favoriteManager.removeFavorite(item)
                            favorites = favoriteManager.getFavorites() // Cập nhật danh sách
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteItem(item: FoodModel, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = item.ImagePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 8.dp)
            )
            Column {
                Text(
                    text = item.Title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.darkPurple)
                )
                Text(
                    text = "${item.Price}đ",
                    fontSize = 14.sp,
                    color = colorResource(R.color.darkPurple)
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.btn_3),
            contentDescription = "Xóa khỏi yêu thích",
            modifier = Modifier
                .size(24.dp)
                .clickable { onRemove() }
        )
    }
}