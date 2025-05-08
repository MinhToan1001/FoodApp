package com.example.foodapp.Activity.Dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodapp.R

@Composable
fun MyBottomBar(
    navController: NavController
) {
    val bottomMenuItemsList = prepareBottomMenu()
    var selectedItem by remember { mutableStateOf("Home") }

    BottomAppBar(
        containerColor = colorResource(R.color.grey),
        tonalElevation = 3.dp
    ) {
        bottomMenuItemsList.forEach { bottomMenuItem ->
            NavigationBarItem(
                selected = selectedItem == bottomMenuItem.label,
                onClick = {
                    selectedItem = bottomMenuItem.label
                    when (bottomMenuItem.label) {
                        "Cart" -> navController.navigate("cart")
                        "Profile" -> navController.navigate("profile")
                        "Home" -> navController.navigate("main")
                        "Order" -> navController.navigate("order")
                        "Favorite" -> navController.navigate("favorite")
                    }
                },
                icon = {
                    Icon(
                        painter = bottomMenuItem.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(20.dp)
                    )
                }
            )
        }
    }
}

data class BottomMenuItem(
    val label: String,
    val icon: Painter
)

@Composable
fun prepareBottomMenu(): List<BottomMenuItem> {
    val bottomMenuItemList = arrayListOf<BottomMenuItem>()
    bottomMenuItemList.add(BottomMenuItem(label = "Home", icon = painterResource(R.drawable.btn_1)))
    bottomMenuItemList.add(BottomMenuItem(label = "Cart", icon = painterResource(R.drawable.btn_2)))
    bottomMenuItemList.add(BottomMenuItem(label = "Favorite", icon = painterResource(R.drawable.btn_3)))
    bottomMenuItemList.add(BottomMenuItem(label = "Order", icon = painterResource(R.drawable.btn_4)))
    bottomMenuItemList.add(BottomMenuItem(label = "Profile", icon = painterResource(R.drawable.btn_5)))

    return bottomMenuItemList
}