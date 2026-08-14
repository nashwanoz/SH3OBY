package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.components.EmptyState
import com.khamrnet.app.ui.components.formatDate

@Composable
fun InvoicesScreen(state: AppUiState, onNewInvoice: () -> Unit) {
    val userNames = state.users.associateBy { it.id }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("فواتير المبيعات", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (state.user?.role == "ADMIN") "عرض جميع فواتير المبيعات المسجلة" else "فواتير المبيعات الخاصة بك",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Button(onClick = onNewInvoice) { Text("فاتورة جديدة") }
        }
        Spacer(Modifier.height(14.dp))
        if (state.invoices.isEmpty()) {
            EmptyState("لا توجد فواتير", "ستظهر الفواتير هنا بعد تسجيل أول عملية بيع")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.invoices, key = { it.id }) { invoice ->
                    val customer = state.customers.firstOrNull { it.id == invoice.customerId }
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(15.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(invoice.id, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("%.2f".format(invoice.total), fontWeight = FontWeight.Bold)
                            }
                            Text("العميل: ${customer?.name ?: "عميل نقدي"}")
                            Text("المستخدم: ${userNames[invoice.userId]?.displayName ?: invoice.userId}", color = Color.Gray)
                            Text("${invoice.paymentType} • ${formatDate(invoice.createdAt)}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}