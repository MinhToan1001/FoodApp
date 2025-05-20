package com.example.foodapp.Activity.Cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.example.foodapp.Domain.FoodModel
import com.example.foodapp.R
import com.uilover.project2142.Helper.ManagmentCart

@Composable
fun CartScreen(
    managmentCart: ManagmentCart = ManagmentCart(LocalContext.current),
    onBackClick: () -> Unit = {},
    navController: NavController? = null,
    onVNPayPayment: (Double) -> Unit
) {
    var cartItems by remember { mutableStateOf<List<FoodModel>>(emptyList()) }
    var tax by remember { mutableStateOf(0.0) }
    var delivery by remember { mutableStateOf(0.0) } // Thêm biến delivery
    var totalAmount by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        managmentCart.getListCart { items ->
            cartItems = items
            calculatorCart(cartItems, managmentCart) { newTax, newDelivery ->
                tax = newTax
                delivery = newDelivery
                totalAmount = cartItems.sumOf { it.Price * it.numberInCart } + tax + delivery
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
                val (backBtn, cartTxt) = createRefs()
                Image(
                    painter = painterResource(id = R.drawable.cart),
                    contentDescription = "Back",
                    modifier = Modifier
                        .clickable {
                            if (navController != null) {
                                navController.popBackStack()
                            } else {
                                onBackClick()
                            }
                        }
                        .constrainAs(backBtn) { start.linkTo(parent.start) }
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(cartTxt) { centerTo(parent) },
                    text = "Giỏ hàng của bạn",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
            }
        }
        if (cartItems.isEmpty()) {
            item {
                Text(
                    text = "Không có đơn hàng nào",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(cartItems) { item ->
                CartItem(
                    cartItems = ArrayList(cartItems),
                    item = item,
                    managmentCart = managmentCart,
                    onItemChange = {
                        managmentCart.getListCart { updatedItems ->
                            cartItems = updatedItems
                            calculatorCart(cartItems, managmentCart) { newTax, newDelivery ->
                                tax = newTax
                                delivery = newDelivery
                                totalAmount = cartItems.sumOf { it.Price * it.numberInCart } + tax + delivery
                            }
                        }
                    }
                )
            }
            item {
                Text(
                    text = "Hóa đơn",
                    color = colorResource(R.color.darkPurple),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            item {
                CartSummary(
                    itemTotal = cartItems.sumOf { it.Price * it.numberInCart },
                    tax = tax,
                    delivery = delivery // Sử dụng delivery động
                )
            }
            item {
                Text(
                    text = "Thông tin",
                    color = colorResource(R.color.darkPurple),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            item {
                DeliveryInfoBox(
                    cartItems = ArrayList(cartItems),
                    totalAmount = totalAmount,
                    managmentCart = managmentCart,
                    navController = navController,
                    onOrderPlaced = {
                        managmentCart.getListCart { updatedItems ->
                            cartItems = updatedItems
                            calculatorCart(cartItems, managmentCart) { newTax, newDelivery ->
                                tax = newTax
                                delivery = newDelivery
                                totalAmount = cartItems.sumOf { it.Price * it.numberInCart } + tax + delivery
                            }
                        }
                    },
                    onVNPayPayment = onVNPayPayment
                )
            }
        }
    }
}

fun calculatorCart(cartItems: List<FoodModel>, managmentCart: ManagmentCart, callback: (Double, Double) -> Unit) {
    val percentTax = 0.02 // Thuế giữ nguyên 2%
    val percentDelivery = 0.08 // Phí vận chuyển 8%
    val totalFee = cartItems.sumOf { it.Price * it.numberInCart } // Tổng giá trị đơn hàng
    val tax = Math.round((totalFee * percentTax) * 100) / 100.0 // Tính thuế
    val delivery = Math.round((totalFee * percentDelivery) * 100) / 100.0 // Tính phí vận chuyển
    callback(tax, delivery) // Trả về cả thuế và phí vận chuyển
}