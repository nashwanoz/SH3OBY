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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
fun CustomersScreen(store: SmartLinkStore) {
    var query by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    val customers = store.customers.filter {
        it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                label = { Text("بحث عن عميل") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة عميل")
            }
        }
        if (customers.isEmpty()) {
            EmptyState(
                title = if (query.isBlank()) "لا يوجد عملاء بعد" else "لا توجد نتائج",
                message = if (query.isBlank()) "أضف عميلًا جديدًا ليظهر هنا" else "جرّب كلمة بحث أخرى",
                actionLabel = if (query.isBlank()) "إضافة عميل" else null,
                onAction = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(customers) { customer ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(customer.name)
                                Text(customer.phone)
                            }
                            Column {
                                Text(formatRiyal(customer.balance))
                                Text("الرصيد", modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone ->
                store.addCustomer(name, phone)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddCustomerDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة عميل") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("اسم العميل") }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, label = { Text("رقم الهاتف") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), phone.trim()) },
                enabled = name.isNotBlank()
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        Text(message, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        if (actionLabel != null) {
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}