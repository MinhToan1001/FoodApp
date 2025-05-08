package com.example.foodapp.Activity.ItemsList

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.ui.theme.FoodAppTheme
import com.example.foodapp.R

class ItemsListActivity : AppCompatActivity() {
    private val viewModel = MainViewModel()
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent.getStringExtra("id") ?: ""
        title = intent.getStringExtra("title") ?: ""
        Log.d("ItemsListActivity", "Category ID: $id, Title: $title")
        enableEdgeToEdge()
        setContent {
            FoodAppTheme {
                ItemsListScreen(
                    title = title,
                    onBackClick = { finish() },
                    viewModel = viewModel,
                    id = id
                )
            }
        }
    }

    @Composable
    fun ItemsListScreen(
        title: String,
        onBackClick: () -> Unit,
        viewModel: MainViewModel,
        id: String
    ) {
        val items by viewModel.foods.observeAsState(emptyList())
        var isLoading by remember { mutableStateOf(true) }
        val filteredItems = items.filter { it.CategoryId == id }

        LaunchedEffect(id) {
            // Dữ liệu đã được tải realtime qua MainViewModel
            isLoading = false
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ConstraintLayout(
                modifier = Modifier.padding(top = 36.dp, start = 16.dp, end = 16.dp)
            ) {
                val (backBtn, cartTxt) = createRefs()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(cartTxt) { centerTo(parent) },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    text = title
                )
                Image(
                    painter = painterResource(R.drawable.back_grey),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable { onBackClick() }
                        .constrainAs(backBtn) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                )
            }
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                filteredItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Không có món ăn nào trong danh mục này")
                    }
                }

                else -> {
                    ItemsList(filteredItems)
                }
            }
        }
    }
}