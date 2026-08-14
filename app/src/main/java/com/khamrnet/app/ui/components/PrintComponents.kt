package com.khamrnet.app.ui.components
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.FinancialBondEntity
import com.khamrnet.app.data.InvoiceEntity
import com.khamrnet.app.data.InvoiceLineEntity
import com.khamrnet.app.ui.BondReceipt
import com.khamrnet.app.ui.SaleReceipt
import com.khamrnet.app.util.PrintAndShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SaleReceiptDialog(receipt: SaleReceipt, canWhatsapp: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("فاتورة مبيعات") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("رقم: ${receipt.invoice.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(formatDate(receipt.invoice.createdAt), fontSize = 10.sp, color = Color.Gray)
                }
                Text("العميل: ${receipt.customer?.name ?: "مبيعات نقدية"}", fontSize = 12.sp)
                Divider()
                receipt.lines.forEach { line ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${line.productName} • ${line.quantity} ${line.unitName}", fontSize = 11.sp)
                        Text("%.2f".format(line.lineTotal), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Divider()
                Text("مبلغ الفاتورة: ${"%.2f".format(receipt.invoice.total)}", fontWeight = FontWeight.Bold)
                Text("رصيدكم السابق: ${"%.2f".format(receipt.invoice.previousBalance)}", fontSize = 11.sp)
                Text(accountBalance(receipt.invoice.newBalance), color = balanceColor(receipt.invoice.newBalance), fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (canWhatsapp && receipt.customer != null) {
                    TextButton(onClick = { PrintAndShare.whatsapp(context, receipt.customer, receipt.invoice) }) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("واتساب")
                    }
                }
                BluetoothPrintButton(
                    invoice = receipt.invoice,
                    customerName = receipt.customer?.name ?: "مبيعات نقدية",
                    lines = receipt.lines
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
fun BondReceiptDialog(receipt: BondReceipt, canWhatsapp: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("سند ${receipt.bond.type} معتمد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("رقم: ${receipt.bond.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(formatDate(receipt.bond.createdAt), fontSize = 10.sp, color = Color.Gray)
                }
                Text("العميل: ${receipt.customer.name}")
                Text("رصيدكم السابق: ${"%.2f".format(receipt.bond.previousBalance)}", fontSize = 12.sp)
                Text("مبلغ السند: ${"%.2f".format(receipt.bond.amount)}", fontWeight = FontWeight.Bold)
                Text(accountBalance(receipt.bond.newBalance), color = balanceColor(receipt.bond.newBalance), fontWeight = FontWeight.Bold)
                Text("البيان: ${receipt.bond.note.ifBlank { "بدون بيان" }}", fontSize = 11.sp)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (canWhatsapp) {
                    TextButton(onClick = { PrintAndShare.whatsappBond(context, receipt.customer, receipt.bond) }) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("واتساب")
                    }
                }
                BluetoothBondPrintButton(receipt.customer, receipt.bond)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
private fun BluetoothPrintButton(
    invoice: InvoiceEntity,
    customerName: String,
    lines: List<InvoiceLineEntity> = emptyList()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPrinters by remember { mutableStateOf(false) }
    var printers by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            printers = PrintAndShare.pairedPrinters()
            showPrinters = true
        } else {
            Toast.makeText(context, "يلزم السماح بالوصول إلى Bluetooth للطباعة", Toast.LENGTH_LONG).show()
        }
    }
    TextButton(onClick = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        else {
            printers = PrintAndShare.pairedPrinters()
            showPrinters = true
        }
    }) {
        Icon(Icons.Default.ReceiptLong, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text("طباعة")
    }
    if (showPrinters) {
        AlertDialog(
            onDismissRequest = { showPrinters = false },
            title = { Text("اختر الطابعة الحرارية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (printers.isEmpty()) {
                        Text("لا توجد طابعة مقترنة. اقترن بالطابعة من إعدادات Bluetooth أولًا.")
                    } else {
                        printers.forEach { device ->
                            OutlinedButton(
                                onClick = {
                                    showPrinters = false
                                    scope.launch(Dispatchers.IO) {
                                        val result = PrintAndShare.printBluetooth(device, invoice, customerName, lines)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                result.exceptionOrNull()?.message ?: "تم إرسال الفاتورة إلى الطابعة",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(device.name ?: device.address) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPrinters = false }) { Text("إغلاق") } }
        )
    }
}

@Composable
private fun BluetoothBondPrintButton(customer: CustomerEntity, bond: FinancialBondEntity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPrinters by remember { mutableStateOf(false) }
    var printers by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            printers = PrintAndShare.pairedPrinters()
            showPrinters = true
        } else {
            Toast.makeText(context, "يلزم السماح بالوصول إلى Bluetooth للطباعة", Toast.LENGTH_LONG).show()
        }
    }
    TextButton(onClick = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        else {
            printers = PrintAndShare.pairedPrinters()
            showPrinters = true
        }
    }) {
        Icon(Icons.Default.ReceiptLong, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text("طباعة")
    }
    if (showPrinters) {
        AlertDialog(
            onDismissRequest = { showPrinters = false },
            title = { Text("اختر الطابعة الحرارية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (printers.isEmpty()) {
                        Text("لا توجد طابعة مقترنة. اقترن بالطابعة من إعدادات Bluetooth أولًا.")
                    } else {
                        printers.forEach { device ->
                            OutlinedButton(
                                onClick = {
                                    showPrinters = false
                                    scope.launch(Dispatchers.IO) {
                                        val result = PrintAndShare.printBondBluetooth(device, customer, bond)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                result.exceptionOrNull()?.message ?: "تم إرسال السند إلى الطابعة",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(device.name ?: device.address) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPrinters = false }) { Text("إغلاق") } }
        )
    }
}
