package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khamrnet.app.vm.POSViewModel
import com.khamrnet.app.data.entities.Invoice
import kotlinx.coroutines.launch
import com.khamrnet.app.util.BluetoothPrinter
import com.khamrnet.app.util.WhatsAppHelper

@Composable
fun InvoiceScreen(invoiceId: Long, vm: POSViewModel) {
    // This screen reads invoice and lines from DB and displays them
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var lines by remember { mutableStateOf<List<com.khamrnet.app.data.entities.InvoiceLine>>(emptyList()) }
    LaunchedEffect(invoiceId) {
        try {
            invoice = vm.repo.db.invoiceDao().findUnposted().firstOrNull { it.id == invoiceId } ?: vm.repo.db.invoiceDao().findUnposted().firstOrNull()
            lines = vm.repo.db.invoiceLineDao().findByInvoice(invoiceId)
        } catch (_: Exception) {}
    }

    Scaffold(topBar = { TopAppBar(title = { Text("عرض الفاتورة") }) }, scaffoldState = scaffoldState) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            invoice?.let { inv ->
                Text(text = "رقم الفاتورة: ${inv.id}", style = MaterialTheme.typography.h6)
                Text(text = "التاريخ: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(inv.createdAt))}", style = MaterialTheme.typography.caption)
                Spacer(Modifier.height(8.dp))
                Divider()
                LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                    items(lines.size) { idx ->
                        val l = lines[idx]
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text(text = "المنتج: ${l.productId} الكمية: ${l.quantity} الإجمالي: ${String.format("%.2f", l.lineTotal)}")
                        }
                        Divider()
                    }
                }
                Divider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("المجموع: ${String.format("%.2f", inv.total)}", style = MaterialTheme.typography.h6)
                    Row {
                        Button(onClick = {
                            // Print invoice
                            val sb = StringBuilder()
                            sb.append("فاتورة رقم ${inv.id}\n")
                            lines.forEach { l ->
                                sb.append("منتج ${l.productId} × ${l.quantity} = ${String.format("%.2f", l.lineTotal)}\n")
                            }
                            sb.append("المجموع: ${String.format("%.2f", inv.total)}\n")
                            BluetoothPrinter.printText(androidx.compose.ui.platform.LocalContext.current, sb.toString()) { ok, err ->
                                coroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(if (ok) "تمت الطباعة" else "خطأ الطباعة: $err")
                                }
                            }
                        }) { Text("طباعة") }

                        Spacer(Modifier.width(8.dp))

                        Button(onClick = {
                            // Share via WhatsApp (dummy customer)
                            WhatsAppHelper.shareInvoice(androidx.compose.ui.platform.LocalContext.current, "عميل", inv.total, 0.0, inv.total)
                        }) { Text("مشاركة واتساب") }
                    }
                }
            } ?: run {
                Text("الفاتورة غير موجودة")
            }
        }
    }
}
