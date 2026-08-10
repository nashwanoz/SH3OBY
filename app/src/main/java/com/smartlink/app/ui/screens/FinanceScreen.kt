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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun FinanceScreen(store: SmartLinkStore) {
    var showDialog by remember { mutableStateOf(false) }
    var entryType by remember { mutableStateOf("قبض") }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    entryType = "قبض"
                    showDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null)
                Text(" قبض")
            }
            Button(
                onClick = {
                    entryType = "صرف"
                    showDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null)
                Text(" صرف")
            }
        }

        if (store.financeEntries.isEmpty()) {
            EmptyState(
                title = "لا توجد حركات مالية",
                message = "سجّل أول سند قبض أو صرف لتظهر حركاتك هنا."
            )
        } else {
            Text("آخر الحركات", modifier = Modifier.padding(top = 28.dp, bottom = 10.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(store.financeEntries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(entry.title)
                                Text(entry.date)
                            }
                            Column {
                                Text(formatRiyal(entry.amount))
                                Text(entry.type)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddFinanceDialog(
            type = entryType,
            onDismiss = { showDialog = false },
            onSave = { title, amount ->
                store.addFinance(title, amount, entryType)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AddFinanceDialog(
    type: String,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val amountValue = amount.toLongOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل سند $type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("البيان") }, singleLine = true)
                OutlinedTextField(
                    amount,
                    { amount = it.filter(Char::isDigit) },
                    label = { Text("المبلغ بالريال") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), amountValue ?: 0) },
                enabled = title.isNotBlank() && amountValue != null && amountValue > 0
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}