package com.smartlink.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartlink.app.ui.SmartLinkStore
import com.smartlink.app.ui.formatRiyal

@Composable
fun InvoiceScreen(store: SmartLinkStore) {
    var showEditor by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showEditor = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" فاتورة جديدة")
            }
            FloatingActionButton(onClick = { showEditor = true }) {
                Icon(Icons.Default.ReceiptLong, contentDescription = "إصدار فاتورة")
            }
        }

        if (store.invoices.isEmpty()) {
            EmptyState(
                title = "لا توجد فواتير",
                message = "ابدأ بإصدار أول فاتورة، وستظهر هنا مباشرة.",
                actionLabel = "إصدار فاتورة",
                onAction = { showEditor = true }
            )
        } else {
            Text(
                "الفواتير المسجلة",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(store.invoices) { invoice ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(invoice.number, style = MaterialTheme.typography.titleMedium)
                                Text(invoice.customer)
                                Text(invoice.date, style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatRiyal(invoice.total), style = MaterialTheme.typography.titleMedium)
                                Text(invoice.type, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        AddInvoiceDialog(
            onDismiss = { showEditor = false },
            onSave = { customer, amount, type ->
                store.addInvoice(customer, amount, type)
                showEditor = false
            }
        )
    }
}

@Composable
private fun AddInvoiceDialog(
    onDismiss: () -> Unit,
    onSave: (String, Long, String) -> Unit
) {
    var customer by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("نقدي") }
    val amountValue = amount.toLongOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إصدار فاتورة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text("اسم العميل (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit) },
                    label = { Text("الإجمالي بالريال") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { type = "نقدي" },
                        enabled = type != "نقدي"
                    ) { Text("نقدي") }
                    TextButton(
                        onClick = { type = "آجل" },
                        enabled = type != "آجل"
                    ) { Text("آجل") }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(customer.trim(), amountValue ?: 0, type) },
                enabled = amountValue != null && amountValue > 0
            ) { Text("إصدار") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}