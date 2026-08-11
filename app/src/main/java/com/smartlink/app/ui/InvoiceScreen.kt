package com.smartlink.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartlink.app.utils.SendWhatsAppDialog
import java.util.*

@Composable
fun InvoiceScreen(defaultCountryCode: String = "967") {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var lastInvoiceNumber by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("اسم العميل") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row {
            Text("+$defaultCountryCode", modifier = Modifier.padding(top = 12.dp))
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = customerPhone,
                onValueChange = { customerPhone = it },
                label = { Text("رقم العميل (مثال: 776323844)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = totalAmount,
            onValueChange = { totalAmount = it },
            label = { Text("مبلغ الفاتورة") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            // save invoice locally (demo): generate invoice number and show WA dialog
            lastInvoiceNumber = generateInvoiceNumber()
            showDialog = true
        }) { Text("حفظ وإصدار الفاتورة") }

        Spacer(Modifier.height(16.dp))
        if (lastInvoiceNumber.isNotEmpty()) Text("آخر فاتورة: $lastInvoiceNumber - $customerName - $totalAmount")
    }

    if (showDialog) {
        SendWhatsAppDialog(
            phone = defaultCountryCode + customerPhone,
            invoiceNumber = lastInvoiceNumber,
            amount = totalAmount,
            onDismiss = { showDialog = false }
        )
    }
}

fun generateInvoiceNumber(): String {
    return UUID.randomUUID().toString().take(8).uppercase(Locale.getDefault())
}