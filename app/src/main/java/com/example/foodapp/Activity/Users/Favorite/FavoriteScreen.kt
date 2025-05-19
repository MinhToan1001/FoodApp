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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.foodapp.Domain.FoodModel
import com.example.foodapp.Helper.FavoriteManager
import com.example.foodapp.R
import com.example.foodapp.ui.theme.FoodAppTheme
import java.text.DecimalFormat

@Composable
fun FavoriteScreen() {
    val context = LocalContext.current
    val favoriteManager = remember { FavoriteManager(context) }
    var favorites by remember { mutableStateOf<List<FoodModel>>(emptyList()) }

    // Tải danh sách yêu thích từ Firebase
    LaunchedEffect(Unit) {
        favoriteManager.getFavorites { items ->
            favorites = items
        }
    }

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
                            favoriteManager.getFavorites { updatedItems ->
                                favorites = updatedItems
                            }
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
                val format = DecimalFormat("#,###")
                val priceFormatted = format.format(item.Price)
                Text(
                    text = "$priceFormatted đ",
                    fontSize = 14.sp,
                    color = colorResource(R.color.red)
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.cross),
            contentDescription = "Xóa khỏi yêu thích",
            modifier = Modifier
                .size(30.dp)
                .clickable { onRemove() }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewFavoriteItem() {
    FoodAppTheme {
        FavoriteItem(
            item = FoodModel(
                Title = "Bánh mì chảo",
                Price = 45000.0,
                ImagePath = "https://via.placeholder.com/150"
            ),
            onRemove = {}
        )
    }
}
