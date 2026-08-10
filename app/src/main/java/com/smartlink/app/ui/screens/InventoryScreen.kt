package com.smartlink.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartlink.app.ui.SmartLinkStore
import com.smartlink.app.ui.formatRiyal

@Composable
fun InventoryScreen(store: SmartLinkStore) {
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("توريد صنف") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    dialogTitle = "توريد صنف"
                    showAddDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AddBox, contentDescription = null)
                Text(" توريد")
            }
            OutlinedButton(
                onClick = {
                    dialogTitle = "إضافة حركة مخزون"
                    showAddDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Text(" حركة")
            }
        }

        if (store.stock.isEmpty()) {
            EmptyState(
                title = "المخزون فارغ",
                message = "أضف أول صنف لتبدأ متابعة الكميات والقيمة.",
                actionLabel = "إضافة صنف",
                onAction = { showAddDialog = true }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(store.stock) { stock ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stock.name, style = MaterialTheme.typography.titleMedium)
                            Text(stock.unit, style = MaterialTheme.typography.bodySmall)
                            Text(
                                stock.quantity.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(top = 22.dp)
                            )
                            Text("الكمية المتاحة", style = MaterialTheme.typography.bodySmall)
                            Text(formatRiyal(stock.value), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddStockDialog(
            title = dialogTitle,
            onDismiss = { showAddDialog = false },
            onSave = { name, unit, quantity, cost ->
                store.addStock(name, unit, quantity, cost)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddStockDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    val quantityValue = quantity.toIntOrNull()
    val costValue = cost.toLongOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("اسم الصنف") }, singleLine = true)
                OutlinedTextField(unit, { unit = it }, label = { Text("الوحدة") }, singleLine = true)
                OutlinedTextField(
                    quantity,
                    { quantity = it.filter(Char::isDigit) },
                    label = { Text("الكمية") },
                    singleLine = true
                )
                OutlinedTextField(
                    cost,
                    { cost = it.filter(Char::isDigit) },
                    label = { Text("سعر الوحدة") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), unit.trim(), quantityValue ?: 0, costValue ?: 0) },
                enabled = name.isNotBlank() && quantityValue != null && quantityValue > 0 &&
                    costValue != null && costValue >= 0
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}