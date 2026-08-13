package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.khamrnet.app.data.entities.Product
import com.khamrnet.app.data.entities.ProductUnit

@Composable
fun ProductUnitQuantityDialog(
    product: Product,
    units: List<ProductUnit>,
    onDismiss: () -> Unit,
    onAdd: (ProductUnit, Double) -> Unit
) {
    var qtyText by remember { mutableStateOf("1") }
    var selectedUnitIndex by remember { mutableStateOf(0) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("إضافة ${product.name}") }, text = {
        Column {
            Text("اختر الوحدة:")
            Spacer(Modifier.height(8.dp))
            units.forEachIndexed { idx, unit ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    RadioButton(selected = idx == selectedUnitIndex, onClick = { selectedUnitIndex = idx })
                    Spacer(Modifier.width(8.dp))
                    Text("${unit.name} (x${unit.multiplier})")
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = qtyText, onValueChange = { qtyText = it }, label = { Text("الكمية") }, singleLine = true)
        }
    }, confirmButton = {
        Button(onClick = {
            val qty = qtyText.toDoubleOrNull() ?: 1.0
            val unit = units.getOrNull(selectedUnitIndex) ?: ProductUnit(id = 0, productId = product.id, name = "حبة", multiplier = 1.0)
            onAdd(unit, qty * unit.multiplier)
        }) {
            Text("أضف إلى الفاتورة")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("إلغاء") }
    })
}
