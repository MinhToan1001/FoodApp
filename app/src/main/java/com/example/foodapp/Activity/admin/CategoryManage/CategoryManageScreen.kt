package com.example.foodapp.Activity.admin.CategoryManage

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.Domain.CategoryModel
import com.example.foodapp.R
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.utils.CloudinaryConfig
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CategoryManageScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val categoriesState by viewModel.categories.observeAsState(mutableListOf())
    val categoryList = categoriesState

    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryModel?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var categoryToDelete by remember { mutableStateOf<CategoryModel?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Quản lý danh mục") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Sử dụng padding từ Scaffold
                .padding(16.dp)
        ) {
            // Header: Nút "Thêm danh mục"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        editingCategory = null
                        showDialog = true
                    }) {
                        Text("Thêm danh mục")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Danh sách các mục
            items(categoryList) { category ->
                CategoryItemRow(
                    category = category,
                    onClick = {
                        editingCategory = it
                        showDialog = true
                    },
                    onDelete = { cat ->
                        categoryToDelete = cat
                    }
                )
            }
        }

        if (showDialog) {
            CategoryDialog(
                initialCategory = editingCategory,
                onDismiss = { showDialog = false },
                onSave = { cat, uri ->
                    isUploading = true
                    coroutineScope.launch {
                        var tempFile: File? = null
                        try {
                            val ref = FirebaseDatabase.getInstance()
                                .getReference("Category")

                            val snapshot = ref.get().await()
                            val maxId = snapshot.children.mapNotNull {
                                it.getValue(CategoryModel::class.java)?.id
                            }.maxOrNull() ?: -1
                            val newId = if (cat.id == 0) maxId + 1 else cat.id

                            val imageUrl = if (uri != null) {
                                tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg").apply {
                                    if (exists()) delete()
                                    createNewFile()
                                }

                                withContext(Dispatchers.IO) {
                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                        tempFile.outputStream().use { output ->
                                            val buffer = ByteArray(4 * 1024)
                                            var bytesRead: Int
                                            var totalBytes = 0L
                                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                                output.write(buffer, 0, bytesRead)
                                                totalBytes += bytesRead
                                            }
                                            output.flush()
                                            Log.d("ImageCopy", "Sao chép $totalBytes bytes từ URI: $uri")
                                        }
                                    } ?: throw IllegalStateException("Không thể mở input stream từ URI: $uri")
                                }

                                if (!tempFile.exists() || tempFile.length() == 0L) {
                                    throw IllegalStateException("File tạm rỗng hoặc không tồn tại: ${tempFile.absolutePath}")
                                }

                                Log.d("CloudinaryUpload", "Chuẩn bị upload file: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")

                                val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                                val activeNetwork = connectivityManager.activeNetworkInfo
                                if (activeNetwork == null || !activeNetwork.isConnected) {
                                    throw IllegalStateException("Không có kết nối internet")
                                }

                                val result = withContext(Dispatchers.IO) {
                                    CloudinaryConfig.cloudinary.uploader().upload(tempFile, mapOf("folder" to "category_images"))
                                }
                                Log.d("CloudinaryUpload", "Upload thành công: $result")
                                result["secure_url"] as String
                            } else {
                                cat.imagePath ?: ""
                            }

                            val updatedCategory = CategoryModel(
                                id = newId,
                                name = cat.name,
                                imagePath = imageUrl
                            )
                            ref.child(newId.toString()).setValue(updatedCategory)
                                .addOnFailureListener { e ->
                                    errorMessage = "Lỗi khi lưu danh mục: ${e.message}"
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

        categoryToDelete?.let { category ->
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa danh mục \"${category.name}\" không?") },
                confirmButton = {
                    Button(onClick = {
                        FirebaseDatabase.getInstance()
                            .getReference("Category")
                            .child(category.id.toString())
                            .removeValue()
                            .addOnSuccessListener {
                                Log.d("FirebaseDelete", "Xóa danh mục thành công: ${category.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseError", "Lỗi khi xóa danh mục: ${e.message}")
                            }
                        categoryToDelete = null
                    }) {
                        Text("Xóa", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { categoryToDelete = null }) {
                        Text("Hủy")
                    }
                }
            )
        }

    }
}
@Composable
fun CategoryItemRow(
    category: CategoryModel,
    onClick: (CategoryModel) -> Unit,
    onDelete: (CategoryModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(category) }
            .padding(vertical = 8.dp)
            .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = category.imagePath,
                contentDescription = "Hình ảnh danh mục",
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 8.dp)
            )
            Text(category.name ?: "Không có tên", fontWeight = FontWeight.Bold)
        }
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Xóa",
            tint = colorResource(id = R.color.red), // Áp dụng màu ở đây
            modifier = Modifier
                .clickable { onDelete(category) }
                .padding(start = 8.dp)
        )

    }
}

@Composable
fun CategoryDialog(
    initialCategory: CategoryModel?,
    onDismiss: () -> Unit,
    onSave: (CategoryModel, Uri?) -> Unit,
    isUploading: Boolean
) {
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("ImageURI", "Selected URI: $uri")
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCategory == null) "Thêm danh mục" else "Chỉnh sửa danh mục") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên danh mục") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Hình ảnh đã chọn",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                    )
                } else if (initialCategory?.imagePath?.isNotEmpty() == true) {
                    AsyncImage(
                        model = initialCategory.imagePath,
                        contentDescription = "Hình ảnh danh mục",
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
                    onSave(
                        CategoryModel(
                            id = initialCategory?.id ?: 0,
                            name = name,
                            imagePath = initialCategory?.imagePath ?: ""
                        ),
                        selectedImageUri
                    )
                },
                enabled = !isUploading
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