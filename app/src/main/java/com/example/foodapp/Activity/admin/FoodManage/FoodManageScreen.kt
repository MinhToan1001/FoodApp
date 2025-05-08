package com.example.foodapp.Activity.admin

import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.Domain.CategoryModel
import com.example.foodapp.Domain.FoodModel
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.utils.CloudinaryConfig
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun FoodManageScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val foodsState by viewModel.foods.observeAsState(mutableListOf())
    val categoriesState by viewModel.categories.observeAsState(mutableListOf())
    val foodList = foodsState
    val categoryList = categoriesState

    var showDialog by remember { mutableStateOf(false) }
    var editingFood by remember { mutableStateOf<FoodModel?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Quản lý món ăn") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        editingFood = null
                        showDialog = true
                    }) {
                        Text("Thêm món ăn")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(foodList) { food ->
                FoodItemRow(
                    food = food,
                    onClick = {
                        editingFood = it
                        showDialog = true
                    },
                    onDelete = { item ->
                        FirebaseDatabase.getInstance()
                            .getReference("Foods")
                            .child(item.Id.toString())
                            .removeValue()
                            .addOnSuccessListener {
                                Log.d("FirebaseDelete", "Xóa món ăn thành công: ${item.Id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseError", "Lỗi khi xóa món ăn: ${e.message}")
                            }
                    }
                )
            }
        }

        if (showDialog) {
            FoodDialog(
                initialFood = editingFood,
                categories = categoryList,
                onDismiss = { showDialog = false },
                onSave = { food, uri ->
                    isUploading = true
                    coroutineScope.launch {
                        var tempFile: File? = null
                        try {
                            val ref = FirebaseDatabase.getInstance()
                                .getReference("Foods")

                            val snapshot = ref.get().await()
                            val maxId = snapshot.children.mapNotNull {
                                it.getValue(FoodModel::class.java)?.Id
                            }.maxOrNull() ?: -1
                            val newId = if (food.Id == 0) maxId + 1 else food.Id

                            val imageUrl = if (uri != null) {
                                tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg").apply {
                                    if (exists()) delete()
                                    createNewFile()
                                }

                                withContext(Dispatchers.IO) {
                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                        tempFile.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    } ?: throw IllegalStateException("Không thể mở input stream từ URI: $uri")
                                }

                                if (!tempFile.exists() || tempFile.length() == 0L) {
                                    throw IllegalStateException("File tạm rỗng hoặc không tồn tại: ${tempFile.absolutePath}")
                                }

                                val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                                val activeNetwork = connectivityManager.activeNetworkInfo
                                if (activeNetwork == null || !activeNetwork.isConnected) {
                                    throw IllegalStateException("Không có kết nối internet")
                                }

                                val result = withContext(Dispatchers.IO) {
                                    CloudinaryConfig.cloudinary.uploader().upload(tempFile, mapOf("folder" to "food_images"))
                                }
                                result["secure_url"] as String
                            } else {
                                food.ImagePath
                            }

                            val updatedFood = food.copy(
                                Id = newId,
                                ImagePath = imageUrl
                            )
                            ref.child(newId.toString()).setValue(updatedFood)
                                .addOnFailureListener { e ->
                                    errorMessage = "Lỗi khi lưu món ăn: ${e.message}"
                                    Log.e("FirebaseError", "Lỗi khi lưu: ${e.message}")
                                }
                        } catch (e: Exception) {
                            errorMessage = "Lỗi khi upload hình ảnh: ${e.message ?: "Không xác định"}"
                            Log.e("CloudinaryUpload", "Lỗi khi upload: ${e.message}", e)
                        } finally {
                            tempFile?.let {
                                if (it.exists()) {
                                    it.delete()
                                    Log.d("FileCleanup", "Đã xóa file tạm: ${it.absolutePath}")
                                }
                            }
                            isUploading = false
                            showDialog = false
                        }
                    }
                },
                isUploading = isUploading
            )
        }

        errorMessage?.let {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Lỗi") },
                text = { Text(it) },
                confirmButton = {
                    Button(onClick = { errorMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun FoodItemRow(
    food: FoodModel,
    onClick: (FoodModel) -> Unit,
    onDelete: (FoodModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(food) }
            .padding(vertical = 8.dp)
            .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = food.ImagePath,
                contentDescription = "Hình ảnh món ăn",
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 8.dp)
            )
            Column {
                Text(food.Title, fontWeight = FontWeight.Bold)
                Text("Số lượng: ${food.numberInCart}", fontSize = 12.sp)
            }
        }
        Text(
            "🗑️",
            modifier = Modifier
                .clickable { onDelete(food) }
                .padding(start = 8.dp)
        )
    }
}

@Composable
fun FoodDialog(
    initialFood: FoodModel?,
    categories: List<CategoryModel>,
    onDismiss: () -> Unit,
    onSave: (FoodModel, Uri?) -> Unit,
    isUploading: Boolean
) {
    var title by remember { mutableStateOf(initialFood?.Title ?: "") }
    var description by remember { mutableStateOf(initialFood?.Description ?: "") }
    var price by remember { mutableStateOf(initialFood?.Price?.toString() ?: "") }
    var timeValue by remember { mutableStateOf(initialFood?.TimeValue?.toString() ?: "") }
    var star by remember { mutableStateOf(initialFood?.Star?.toString() ?: "") }
    var calorie by remember { mutableStateOf(initialFood?.Calorie?.toString() ?: "") }
    var numberInCart by remember { mutableStateOf(initialFood?.numberInCart?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(initialFood?.CategoryId ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialFood == null) "Thêm món ăn" else "Chỉnh sửa món ăn") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên món ăn") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Giá") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = timeValue,
                    onValueChange = { timeValue = it },
                    label = { Text("Thời gian chuẩn bị (phút)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = star,
                    onValueChange = { star = it },
                    label = { Text("Đánh giá (sao)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = calorie,
                    onValueChange = { calorie = it },
                    label = { Text("Calo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = numberInCart,
                    onValueChange = { numberInCart = it },
                    label = { Text("Số lượng trong kho") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Dropdown chọn danh mục
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = categories.find { it.id.toString() == selectedCategoryId }?.name ?: "Chọn danh mục",
                        onValueChange = {},
                        label = { Text("Danh mục") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedCategoryId = category.id.toString()
                                    expanded = false
                                }
                            ) {
                                Text(category.name ?: "Không có tên")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Hình ảnh đã chọn",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                    )
                } else if (initialFood?.ImagePath?.isNotEmpty() == true) {
                    AsyncImage(
                        model = initialFood.ImagePath,
                        contentDescription = "Hình ảnh món ăn",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                    )
                }
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Text("Chọn hình ảnh")
                }
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && price.isNotBlank() && selectedCategoryId.isNotBlank()) {
                        onSave(
                            FoodModel(
                                Id = initialFood?.Id ?: 0,
                                Title = title,
                                Description = description,
                                Price = price.toDoubleOrNull() ?: 0.0,
                                TimeValue = timeValue.toIntOrNull() ?: 0,
                                Star = star.toDoubleOrNull() ?: 0.0,
                                Calorie = calorie.toIntOrNull() ?: 0,
                                numberInCart = numberInCart.toIntOrNull() ?: 0,
                                CategoryId = selectedCategoryId,
                                ImagePath = initialFood?.ImagePath ?: ""
                            ),
                            selectedImageUri
                        )
                    }
                },
                enabled = !isUploading && title.isNotBlank() && price.isNotBlank() && selectedCategoryId.isNotBlank()
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("Hủy")
            }
        }
    )
}