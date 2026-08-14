package com.khamrnet.app.ui.screens
import com.khamrnet.app.AppSection

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.CustomerStatementRow
import com.khamrnet.app.ui.AppSection
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.AppViewModel
import com.khamrnet.app.ui.components.EmptyState
import com.khamrnet.app.ui.components.FormField
import com.khamrnet.app.ui.components.accountBalance
import com.khamrnet.app.ui.components.balanceColor
import com.khamrnet.app.ui.components.formatDate
import com.khamrnet.app.util.PrintAndShare

@Composable
fun CustomersScreen(
    state: AppUiState,
    viewModel: AppViewModel,
    onIssueInvoice: (CustomerEntity) -> Unit,
    onIssueBond: (CustomerEntity) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var statementCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var statementRows by remember { mutableStateOf<List<CustomerStatementRow>?>(null) }
    val nextCustomerCode = (state.customers.mapNotNull { it.customerCode.toIntOrNull() }.maxOrNull() ?: 0) + 1
    val matches = state.customers.filter {
        query.isNotBlank() && (
            it.name.contains(query.trim(), ignoreCase = true) ||
                it.mobile.contains(query.trim(), ignoreCase = true)
            )
    }
    val context = LocalContext.current
    val canWhatsapp = state.user?.role == "ADMIN" || state.user?.canWhatsapp == true
    val canIssueInvoice = state.user?.canAccess(AppSection.POS.name) == true
    val canIssueBond = state.user?.canAccess(AppSection.BONDS.name) == true
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("العملاء", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("ابحث بالاسم ثم اختر العملية المطلوبة", color = Color.Gray, fontSize = 12.sp)
            }
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "إضافة") }
        }
        Spacer(Modifier.height(14.dp))
        FormField("ابحث باسم العميل أو رقم الجوال", query) { query = it }
        Spacer(Modifier.height(10.dp))
        when {
            state.customers.isEmpty() -> EmptyState("لا يوجد عملاء", "أضف أول عميل لاستخدام البيع الآجل")
            query.isBlank() -> Text("اكتب جزءًا من اسم العميل أو رقم الجوال لعرضه", color = Color.Gray)
            matches.isEmpty() -> EmptyState("لا توجد نتائج", "جرّب كتابة جزء آخر من اسم العميل")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(matches) { customer ->
                    Card(
                        Modifier.fillMaxWidth().clickable { selectedCustomer = customer },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    Text(customer.mobile.ifBlank { "لا يوجد رقم هاتف" }, color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(accountBalance(customer.balance), color = balanceColor(customer.balance), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (canIssueInvoice) {
                                    OutlinedButton(
                                        onClick = { onIssueInvoice(customer) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("إصدار فاتورة", fontSize = 11.sp) }
                                }
                                if (canIssueBond) {
                                    OutlinedButton(
                                        onClick = { onIssueBond(customer) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("سند قبض", fontSize = 11.sp) }
                                }
                                OutlinedButton(
                                    onClick = {
                                        statementCustomer = customer
                                        statementRows = null
                                        viewModel.loadCustomerStatement(customer.id) { rows -> statementRows = rows }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text("كشف حساب", fontSize = 11.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
    selectedCustomer?.let { customer ->
        CustomerSummaryDialog(
            customer = customer,
            lastMovement = state.customerLastMovement[customer.id],
            onStatement = {
                selectedCustomer = null
                statementCustomer = customer
                statementRows = null
                viewModel.loadCustomerStatement(customer.id) { rows -> statementRows = rows }
            },
            onDismiss = { selectedCustomer = null }
        )
    }
    if (statementCustomer != null && statementRows != null) {
        CustomerStatementDialog(
            customer = statementCustomer!!,
            rows = statementRows!!,
            canWhatsapp = canWhatsapp,
            onSharePdf = { PrintAndShare.shareStatement(context, statementCustomer!!, statementRows!!) },
            onShareWhatsapp = { PrintAndShare.shareStatementToWhatsapp(context, statementCustomer!!, statementRows!!) },
            onDismiss = {
                statementCustomer = null
                statementRows = null
            }
        )
    }
    if (showAdd) AddCustomerDialog(viewModel, nextCustomerCode.toString()) { showAdd = false }
}

@Composable
private fun AddCustomerDialog(viewModel: AppViewModel, defaultCode: String, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var customerCode by remember { mutableStateOf(defaultCode) }
    var mobile by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("عميل جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("اسم العميل", name) { name = it }
                FormField("كود العميل الفريد", customerCode, numeric = true) { customerCode = it }
                FormField("رقم الجوال", mobile, numeric = true) { mobile = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.addCustomer(name, customerCode, mobile); onDismiss() },
                enabled = name.isNotBlank() && customerCode.isNotBlank()
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun CustomerStatementDialog(
    customer: CustomerEntity,
    rows: List<CustomerStatementRow>,
    canWhatsapp: Boolean,
    onSharePdf: () -> Unit,
    onShareWhatsapp: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("كشف حساب ${customer.name}") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(accountBalance(customer.balance), color = balanceColor(customer.balance), fontWeight = FontWeight.Bold)
                if (rows.isEmpty()) {
                    Text("لا توجد حركات مالية لهذا العميل", color = Color.Gray)
                } else {
                    rows.forEach { row ->
                        Card(shape = RoundedCornerShape(10.dp)) {
                            Column(Modifier.fillMaxWidth().padding(9.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(row.type, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(formatDate(row.createdAt), fontSize = 10.sp, color = Color.Gray)
                                }
                                Text("المرجع: ${row.reference}", fontSize = 11.sp, color = Color.Gray)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("المبلغ: ${"%.2f".format(row.amount)}", fontSize = 11.sp)
                                    Text(accountBalance(row.balanceAfter), fontSize = 11.sp, color = balanceColor(row.balanceAfter))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onSharePdf) { Text("PDF", fontSize = 11.sp) }
                if (canWhatsapp) {
                    TextButton(onClick = onShareWhatsapp) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(3.dp))
                        Text("WhatsApp", fontSize = 11.sp)
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
private fun CustomerSummaryDialog(
    customer: CustomerEntity,
    lastMovement: Long?,
    onStatement: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(customer.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(accountBalance(customer.balance), fontWeight = FontWeight.Bold, color = balanceColor(customer.balance))
                Text("رقم الجوال: ${customer.mobile.ifBlank { "غير متوفر" }}")
                Text("آخر حركة: ${lastMovement?.let(::formatDate) ?: "لا توجد حركة"}", color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = onStatement) {
                Icon(Icons.Default.Assessment, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("توليد كشف الحساب")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}
