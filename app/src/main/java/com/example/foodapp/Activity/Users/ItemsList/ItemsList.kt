package com.example.foodapp.Activity.ItemsList

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import coil.compose.AsyncImage
import com.example.foodapp.Activity.DetailEachFood.DetailEachFoodActivity
import com.example.foodapp.Activity.Splash.PlayWriteFontFamily
import com.example.foodapp.Domain.FoodModel
import com.example.foodapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat

@Composable
fun ItemsList(items: List<FoodModel>) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        itemsIndexed(items) { index, item ->
            Items(item = item, index = index)
        }
    }
}

@Composable
fun Items(item: FoodModel, index: Int) {
    val isEvenRow = index % 2 == 0
    val context = LocalContext.current
    var averageRating by remember { mutableStateOf(item.Star) }

    // Lấy dữ liệu đánh giá từ Firebase
    LaunchedEffect(item.Title) {
        FirebaseDatabase.getInstance().getReference("products")
            .child(item.Title)
            .child("ratings")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ratings = snapshot.children.mapNotNull { it.getValue(Rating::class.java) }
                    averageRating = if (ratings.isNotEmpty()) {
                        ratings.map { it.stars }.average()
                    } else {
                        item.Star
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
            .padding(8.dp)
            .clickable {
                val intent = Intent(context, DetailEachFoodActivity::class.java).apply {
                    putExtra("object", item)
                }
                startActivity(context, intent, null)
            }
    ) {
        if (isEvenRow) {
            FoodImage(item = item)
            FoodDetails(item = item, averageRating = averageRating)
        } else {
            FoodDetails(item = item, averageRating = averageRating)
            FoodImage(item = item)
        }
    }
}

@Composable
fun FoodImage(item: FoodModel) {
    AsyncImage(
        model = item.ImagePath,
        contentDescription = "Hình ảnh món ${item.Title}",
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colorResource(R.color.grey), shape = RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun RowScope.FoodDetails(item: FoodModel, averageRating: Double) {
    Column(
        modifier = Modifier
            .padding(start = 12.dp, end = 8.dp)
            .weight(1f)
            .align(Alignment.CenterVertically)
    ) {
        Text(
            text = item.Title,
            color = colorResource(R.color.darkPurple),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TimingRow(item.TimeValue)
        RatingBarRow(averageRating)
        QuantityRow(item.numberInCart)
        Text(
            text = formatPrice(item.Price) + "đ",
            color = colorResource(R.color.red),
            fontSize = 18.sp,
            fontFamily = PlayWriteFontFamily,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun RatingBarRow(star: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.star),
            contentDescription = "Đánh giá",
            modifier = Modifier
                .size(16.dp)
                .padding(end = 4.dp)
        )
        Text(
            text = String.format("%.1f", star),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun TimingRow(timeValue: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.time),
            contentDescription = "Thời gian chuẩn bị",
            modifier = Modifier
                .size(16.dp)
                .padding(end = 4.dp)
        )
        Text(
            text = "$timeValue phút",
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun QuantityRow(quantity: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "SL: $quantity",
            style = MaterialTheme.typography.body1
        )
    }
}

fun formatPrice(price: Double): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(price.toInt())
}

data class Rating(
    val stars: Int = 0,
    val comment: String = "",
    val userId: String = "",
    val fullName: String = "",
    val timestamp: Long = 0L
)
@Preview(showBackground = true)
@Composable
fun PreviewItemsList() {
    val fakeItems = listOf(
        FoodModel(
            Title = "Phở Bò",
            Description = "Phở bò truyền thống ngon tuyệt",
            Price = 50000.0,
            ImagePath = "https://via.placeholder.com/150",
            Star = 4.5,
            TimeValue = 15,
            numberInCart = 10
        ),
        FoodModel(
            Title = "Bún Chả",
            Description = "Bún chả Hà Nội đậm đà",
            Price = 45000.0,
            ImagePath = "https://via.placeholder.com/150",
            Star = 4.0,
            TimeValue = 20,
            numberInCart = 8
        ),
        FoodModel(
            Title = "Cơm Tấm",
            Description = "Cơm tấm sườn bì chả",
            Price = 55000.0,
            ImagePath = "https://via.placeholder.com/150",
            Star = 4.8,
            TimeValue = 12,
            numberInCart = 5
        )
    )
    ItemsList(items = fakeItems)
}