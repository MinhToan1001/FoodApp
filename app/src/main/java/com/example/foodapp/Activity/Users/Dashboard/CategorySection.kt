package com.example.foodapp.Activity.Dashboard

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.Activity.ItemsList.ItemsListActivity
import com.example.foodapp.Domain.CategoryModel
import com.example.foodapp.R

@Composable
fun CategorySection(categories: List<CategoryModel>, showCategoryLoading: Boolean) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Chọn danh mục",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (showCategoryLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không có danh mục nào",
                    fontSize = 16.sp,
                    color = colorResource(R.color.darkPurple)
                )
            }
        } else {
            Log.d("CategorySection", "Hiển thị ${categories.size} danh mục: ${categories.map { it.name }}")
            val rows = categories.chunked(3)
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { categoryModel ->
                        CategoryItem(
                            category = categoryModel,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            onItemClick = {
                                val intent = Intent(context, ItemsListActivity::class.java).apply {
                                    putExtra("id", categoryModel.id.toString())
                                    putExtra("title", categoryModel.name ?: "Không có tên")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                    if (row.size < 3) {
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: CategoryModel, modifier: Modifier = Modifier, onItemClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.lightOrange),
                shape = RoundedCornerShape(13.dp)
            )
            .clickable(onClick = onItemClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = category.imagePath.takeIf { !it.isNullOrEmpty() } ?: "https://via.placeholder.com/80",
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = category.name.takeIf { !it.isNullOrEmpty() } ?: "Không có tên",
            color = colorResource(R.color.darkPurple),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}