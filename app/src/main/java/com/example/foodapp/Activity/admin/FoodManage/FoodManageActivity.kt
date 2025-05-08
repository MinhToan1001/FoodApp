package com.example.foodapp.Activity.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.ui.theme.FoodAppTheme

class FoodManageActivity : ComponentActivity() {
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodAppTheme {
                FoodManageActivityScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FoodManageActivityScreen(viewModel: MainViewModel) {
    var selectedIndex by remember { mutableStateOf(1) } // Mặc định chọn tab quản lý món ăn (btn_3)

    Scaffold(
        bottomBar = {
            AdminBottomBar(
                selectedIndex = selectedIndex,
                onItemClick = { index ->
                    selectedIndex = index
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedIndex) {
                1 -> FoodManageScreen(viewModel = viewModel) // Hiển thị FoodManageScreen khi chọn btn_3
                // Các tab khác có thể thêm sau
                else -> Text("Chưa triển khai")
            }
        }
    }
}