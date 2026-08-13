package com.khamrnet.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khamrnet.app.util.BluetoothPrinter
import com.khamrnet.app.util.WhatsAppHelper
import com.khamrnet.app.vm.POSViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.khamrnet.app.data.entities.Product
import com.khamrnet.app.data.entities.ProductUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun POSFastScreen(vm: POSViewModel, onBack: () -> Unit) {
    val products by vm.products.collectAsState()
    val lines by vm.currentLines.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    var showProductDialog by remember { mutableStateOf<Pair<Product, List<ProductUnit>>?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("نقطة بيع سريعة") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") }
        })
    }, scaffoldState = scaffoldState) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Product grid 2 columns, large buttons
            val display = if (products.size >= 5) products.take(5) else products
            LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(display) { p ->
                    Card(modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .clickable {
                            selectedProduct = p
                            // load units
                            coroutineScope.launch {
                                val units = vm.repo.db.productUnitDao().findByProduct(p.id)
                                showProductDialog = Pair(p, units)
                            }
                        }, elevation = 6.dp) {
                        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = p.name, style = MaterialTheme.typography.h6)
                            Spacer(Modifier.height(6.dp))
                            Text(text = "السعر: ${String.format("%.2f", p.price)}", style = MaterialTheme.typography.body2)
                        }
                    }
                }
            }

            // Invoice summary and action buttons
            Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("قائمة الفاتورة", style = MaterialTheme.typography.subtitle1)
                    Spacer(Modifier.height(8.dp))
                    if (lines.isEmpty()) {
                        Text("لا توجد عناصر", style = MaterialTheme.typography.body2)
                    } else {
                        lines.forEachIndexed { idx, it ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("${it.productName} - ${it.unitName}", style = MaterialTheme.typography.body1)
                                    Text("الكمية: ${it.quantity}  السعر الإجمالي: ${String.format("%.2f", it.lineTotal)}", style = MaterialTheme.typography.caption)
                                }
                                IconButton(onClick = { vm.removeLine(idx) }) {
                                    Text("حذف", color = Color.Red)
                                }
                            }
                        }
                        Divider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المجموع:", style = MaterialTheme.typography.h6)
                            Text(String.format("%.2f", vm.getTotal()), style = MaterialTheme.typography.h6)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(onClick = {
                            // Demo print: prints invoice summary as Arabic text
                            val txt = buildString {
                                append("فاتورة\n")
                                append("--------------------\n")
                                lines.forEach { l ->
                                    append("${l.productName} ${l.unitName} x${l.quantity} = ${String.format("%.2f", l.lineTotal)}\n")
                                }
                                append("--------------------\n")
                                append("المجموع: ${String.format("%.2f", vm.getTotal())}\n")
                            }
                            BluetoothPrinter.printText(
                                context = androidx.compose.ui.platform.LocalContext.current,
                                text = txt
                            ) { ok, err ->
                                coroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(if (ok) "تمت الطباعة" else "فشل الطباعة: $err")
                                }
                            }
                        }) {
                            Text("طباعة")
                        }

                        Button(onClick = {
                            // WhatsApp share using template. Customer fields are unknown in this POS screen; share generic.
                            val message = buildString {
                                append("عزيزي العميل: عميل نقدي\n")
                                append("قيمة الفاتورة الحالية: ${String.format("%.2f", vm.getTotal())}\n")
                                append("رصيدك السابق: 0.00\n")
                                append("الإجمالي المستحق: ${String.format("%.2f", vm.getTotal())}")
                            }
                            WhatsAppHelper.shareInvoice(
                                context = androidx.compose.ui.platform.LocalContext.current,
                                customerName = "عميل نقدي",
                                amount = vm.getTotal(),
                                prevBalance = 0.0,
                                newTotal = vm.getTotal()
                            )
                        }) {
                            Text("مشاركة واتساب")
                        }

                        Button(onClick = {
                            // Finalize invoice: for demo we assume cashier id = 1 (default admin) unless explicit login integration is used
                            val cashierId = 1L
                            vm.finalizeInvoice(cashierId = cashierId, customerId = null) { success, invId, error ->
                                coroutineScope.launch {
                                    if (success) {
                                        scaffoldState.snackbarHostState.showSnackbar("تم إنشاء الفاتورة برقم $invId")
                                    } else {
                                        scaffoldState.snackbarHostState.showSnackbar("فشل: $error")
                                    }
                                }
                            }
                        }) {
                            Text("إنهاء الفاتورة")
                        }
                    }
                }
            }
        }
    }

    // Product->Unit->Quantity dialog
    if (showProductDialog != null) {
        val pair = showProductDialog!!
        ProductUnitQuantityDialog(product = pair.first, units = pair.second, onDismiss = { showProductDialog = null }, onAdd = { unit, qty ->
            vm.addLine(product = pair.first, unit = unit, qty = qty)
            showProductDialog = null
        })
    }
}
