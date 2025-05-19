package com.example.foodapp.Activity.DetailEachFood

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.foodapp.Domain.FoodModel
import com.example.foodapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uilover.project2142.Helper.ManagmentCart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailEachFoodActivity : AppCompatActivity() {
    private lateinit var item: FoodModel
    private lateinit var managmentCart: ManagmentCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = intent.getSerializableExtra("object") as FoodModel
        item.numberInCart = 1
        managmentCart = ManagmentCart(this)
        setContent {
            DetailScreen(
                item = item,
                onBackClick = { finish() },
                onAddToCartClick = { managmentCart.insertItem(item) }
            )
        }
    }
}

@Composable
private fun DetailScreen(
    item: FoodModel,
    onBackClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    var numberInCart by remember { mutableStateOf(item.numberInCart) }
    val database = FirebaseDatabase.getInstance().getReference("ratings")
    var ratings by remember { mutableStateOf(listOf<Rating>()) }
    var averageStars by remember { mutableStateOf(0.0) }

    // Lắng nghe thay đổi dữ liệu đánh giá từ Firebase
    LaunchedEffect(item.Title) {
        Log.d("DetailEachFood", "Đang lấy đánh giá cho sản phẩm: ${item.Title}")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ratingList = mutableListOf<Rating>()
                for (data in snapshot.children) {
                    val rating = data.getValue(Rating::class.java)
                    if (rating != null) {
                        Log.d("DetailEachFood", "Tìm thấy đánh giá: productTitle=${rating.productTitle}, stars=${rating.stars}")
                        if (rating.productTitle.equals(item.Title, ignoreCase = true)) {
                            ratingList.add(rating)
                        }
                    }
                }
                ratings = ratingList
                averageStars = if (ratingList.isNotEmpty()) {
                    ratingList.map { it.stars }.average()
                } else {
                    0.0
                }
                Log.d("DetailEachFood", "Tổng số đánh giá tìm thấy: ${ratingList.size}, Số sao trung bình: $averageStars")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DetailEachFood", "Lỗi lấy đánh giá: ${error.message}")
            }
        })
    }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (footer, column) = createRefs()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .constrainAs(column) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                }
                .padding(bottom = 80.dp)
        ) {
            HeaderSection(
                item = item,
                numberInCart = numberInCart,
                averageStars = averageStars,
                onBackClick = onBackClick,
                onIncrement = {
                    numberInCart++
                    item.numberInCart = numberInCart
                },
                onDecrement = {
                    if (numberInCart > 1) {
                        numberInCart--
                        item.numberInCart = numberInCart
                    }
                }
            )
            FoodDescriptionSection(item.Description)
            RatingSection(ratings, averageStars)
        }
        FooterSection(
            onAddToCartClick,
            totalPrice = (item.Price * numberInCart),
            Modifier.constrainAs(footer) {
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
            }
        )
    }
}

@Composable
fun RatingSection(ratings: List<Rating>, averageStars: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Đánh giá sản phẩm",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.darkPurple)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Số sao trung bình: ",
                fontSize = 16.sp,
                color = colorResource(R.color.darkPurple)
            )
            Text(
                text = String.format("%.1f", averageStars),
                fontSize = 16.sp,
                color = colorResource(R.color.darkPurple),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Row {
                for (i in 1..5) {
                    Text(
                        text = "★",
                        color = if (i <= averageStars) Color.Yellow else Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(Dựa trên ${ratings.size} đánh giá)",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        if (ratings.isEmpty()) {
            Text(
                text = "Chưa có đánh giá nào",
                fontSize = 16.sp,
                color = colorResource(R.color.darkPurple)
            )
        } else {
            // Thay LazyColumn bằng Column
            Column {
                ratings.forEach { rating ->
                    RatingItem(rating)
                }
            }
        }
    }
}

@Composable
fun RatingItem(rating: Rating) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = rating.fullName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.darkPurple)
        )
        Row {
            for (i in 1..rating.stars) {
                Text(
                    text = "★",
                    color = Color.Yellow,
                    fontSize = 16.sp
                )
            }
        }
        Text(
            text = rating.comment,
            fontSize = 14.sp,
            color = colorResource(R.color.darkPurple),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "Ngày đánh giá: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(rating.timestamp))}",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun FoodDescriptionSection(description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Mô tả",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.darkPurple)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

data class Rating(
    val stars: Int = 0,
    val comment: String = "",
    val productTitle: String = "",
    val userId: String = "",
    val fullName: String = "",
    val timestamp: Long = 0L
)
