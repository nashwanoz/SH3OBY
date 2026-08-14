package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khamrnet.app.data.ProductEntity
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.AppViewModel
import com.khamrnet.app.ui.components.FormField

@Composable
fun ProductsScreen(state: AppUiState, viewModel: AppViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("بيانات الأصناف", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("الأسعار تُقفل على الكاشير حسب إعدادات المدير", color = Color.Gray, fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = viewModel::testFirebaseConnection,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) { Text("اختبار Firebase", fontSize = 11.sp) }
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "إضافة") }
        }
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.products) { product ->
                Card(Modifier.fillMaxWidth().height(118.dp), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        Modifier.padding(10.dp).fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("باركود: ${product.barcode.ifBlank { "غير محدد" }}", color = Color.Gray, fontSize = 12.sp)
                            Text("${"%.2f".format(product.price)} / ${product.unitName}", color = MaterialTheme.colorScheme.primary)
                            Text("${"%.2f".format(product.casePrice)} / ${product.caseUnitName}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(onClick = { editingProduct = product }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل الصنف")
                            }
                            IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف الصنف", tint = Color(0xFFB3261E))
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAdd) ProductEditorDialog(null, viewModel) { showAdd = false }
    editingProduct?.let { product ->
        ProductEditorDialog(product, viewModel) { editingProduct = null }
    }
}

@Composable
private fun ProductEditorDialog(product: ProductEntity?, viewModel: AppViewModel, onDismiss: () -> Unit) {
    var name by remember(product?.id) { mutableStateOf(product?.name ?: "") }
    var barcode by remember(product?.id) { mutableStateOf(product?.barcode ?: "") }
    var unit by remember(product?.id) { mutableStateOf(product?.unitName ?: "حبة") }
    var price by remember(product?.id) { mutableStateOf(product?.price?.toString() ?: "") }
    var caseName by remember(product?.id) { mutableStateOf(product?.caseUnitName ?: "كرت") }
    var caseQty by remember(product?.id) { mutableStateOf(product?.caseQuantity?.toString() ?: "60") }
    var casePrice by remember(product?.id) { mutableStateOf(product?.casePrice?.toString() ?: "") }
    var stock by remember(product?.id) { mutableStateOf("0") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "صنف جديد" else "تعديل الصنف") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("اسم الصنف", name) { name = it }
                FormField("الباركود", barcode, numeric = true) { barcode = it }
                FormField("الوحدة المفردة", unit) { unit = it }
                FormField("سعر المفردة", price, numeric = true) { price = it }
                FormField("الوحدة الكبيرة", caseName) { caseName = it }
                FormField("سعة الوحدة", caseQty, numeric = true) { caseQty = it }
                FormField("سعر الوحدة الكبيرة", casePrice, numeric = true) { casePrice = it }
                if (product == null) FormField("رصيد المستودع الرئيسي", stock, numeric = true) { stock = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = price.toDoubleOrNull() ?: 0.0
                    val parsedCaseQty = caseQty.toIntOrNull() ?: 1
                    val parsedCasePrice = casePrice.toDoubleOrNull() ?: 0.0
                    if (product == null) {
                        viewModel.createProduct(name, barcode, unit, parsedPrice, caseName, parsedCaseQty, parsedCasePrice, stock.toIntOrNull() ?: 0)
                    } else {
                        viewModel.updateProduct(
                            product.copy(
                                name = name,
                                barcode = barcode,
                                unitName = unit,
                                price = parsedPrice,
                                caseUnitName = caseName,
                                caseQuantity = parsedCaseQty,
                                casePrice = parsedCasePrice
                            )
                        )
                    }
                    onDismiss()
                },
                enabled = name.isNotBlank() && (price.toDoubleOrNull() ?: 0.0) > 0
            ) { Text(if (product == null) "حفظ" else "تحديث") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
