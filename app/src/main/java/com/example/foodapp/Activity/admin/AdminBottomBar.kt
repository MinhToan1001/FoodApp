package com.example.foodapp.Activity.admin

import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.foodapp.R

@Composable
fun AdminBottomBar(
    selectedIndex: Int,
    onItemClick: (Int) -> Unit
) {
    val bottomMenuItems = prepareAdminBottomMenu()

    BottomAppBar(
        backgroundColor = Color(0xFFE0E0E0),
        elevation = 4.dp
    ) {
        bottomMenuItems.forEachIndexed { index, menuItem ->
            BottomNavigationItem(
                selected = selectedIndex == index,
                onClick = { onItemClick(index) },
                icon = {
                    Icon(
                        painter = menuItem.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                selectedContentColor = Color(0xFF6200EE),
                unselectedContentColor = Color.Gray
            )
        }
    }
}

data class AdminBottomMenuItem(
    val icon: Painter
)

@Composable
fun prepareAdminBottomMenu(): List<AdminBottomMenuItem> {
    return listOf(
        AdminBottomMenuItem(
            icon = painterResource(R.drawable.btn_1)
        ),
        AdminBottomMenuItem(
            icon = painterResource(R.drawable.btn_3)
        ),
        AdminBottomMenuItem(
            icon = painterResource(R.drawable.btn_4)
        ),
        AdminBottomMenuItem(
            icon = painterResource(R.drawable.credit_card)
        ),
    )
}