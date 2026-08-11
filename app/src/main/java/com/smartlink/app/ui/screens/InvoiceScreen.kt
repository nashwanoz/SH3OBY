package com.smartlink.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartlink.app.ui.sampleInvoices

@Composable
fun InvoiceScreen() {
    var selectedType by remember { mutableStateOf("نقدي") }
    var showEditor by remember { mutableStateOf(false) }
    var customerSearch by remember { mutableStateOf("") }

    if (showEditor) {
        InvoiceEditor(
            type = selectedType,
            customerSearch = customerSearch,
            onCustomerSearchChange = { customerSearch = it },
            onSave = { showEditor = false },
            onBack = { showEditor = false }
        )
        return
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    selectedType = "نقدي"
                    showEditor = true
                },
                modifier = Modifier.weight(1f)
            ) { Text("فاتورة نقدي") }
            Button(
                onClick = {
                    selectedType = "آجل"
                    showEditor = true
                },
                modifier = Modifier.weight(1f)
            ) { Text("فاتورة آجل") }
        }
        Text(
            "آخر الفواتير",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sampleInvoices) { invoice ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(invoice.number, style = MaterialTheme.typography.titleMedium)
                            Text(invoice.customer, style = MaterialTheme.typography.bodyMedium)
                            Text(invoice.date, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text(invoice.total, style = MaterialTheme.typography.titleMedium)
                            Text(invoice.type, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceEditor(
    type: String,
    customerSearch: String,
    onCustomerSearchChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("AH-125", style = MaterialTheme.typography.titleLarge)
            Text("اليوم، ١٢:٠٥ م", style = MaterialTheme.typography.bodyMedium)
        }
        if (type == "آجل") {
            OutlinedTextField(
                value = customerSearch,
                onValueChange = onCustomerSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                label = { Text("اسم العميل") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة عميل")
                    }
                },
                singleLine = true
            )
            Text("الرصيد السابق: ٢٨٥,٠٠٠ ر.ي", style = MaterialTheme.typography.bodySmall)
        } else {
            Text("مبيعات نقدية", modifier = Modifier.padding(top = 20.dp))
        }
        Text(
            "تفاصيل الفاتورة",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("اضغط إضافة لإدخال صنف")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = true, onClick = {}, label = { Text("مياه معدنية") })
                    FilterChip(selected = false, onClick = {}, label = { Text("كرتون") })
                }
                Text("الإجمالي: ٠ ر.ي", modifier = Modifier.padding(top = 18.dp))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("إصدار الفاتورة") }
            androidx.compose.material3.OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("إلغاء")
            }
        }
        androidx.compose.material3.OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Icon(Icons.Default.Print, contentDescription = null)
            Text(" معاينة وطباعة")
        }
    }
}