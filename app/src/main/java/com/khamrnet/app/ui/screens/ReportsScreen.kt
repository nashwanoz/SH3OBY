package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.components.accountBalance
import com.khamrnet.app.ui.components.balanceColor

@Composable
fun ReportsScreen(state: AppUiState) {
    val visibleUsers = if (state.user?.role == "ADMIN") state.users else state.users.filter { it.id == state.user?.id }
    val salesByUser = state.invoices.groupingBy { it.userId }.fold(0.0) { total, invoice -> total + invoice.total }
    val invoiceCountByUser = state.invoices.groupingBy { it.userId }.eachCount()
    val userNames = state.users.associateBy { it.id }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("التقارير", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("ملخص حركة المبيعات والعهدة حسب المستخدم", color = Color.Gray, fontSize = 12.sp)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F3EF))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("إجمالي المبيعات المعروضة", color = Color.Gray)
                        Text("%.2f".format(state.invoices.sumOf { it.total }), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("${state.invoices.size} فاتورة", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
            item { Text("تقرير المستخدمين", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            items(visibleUsers, key = { it.id }) { user ->
                val sales = salesByUser[user.id] ?: 0.0
                val count = invoiceCountByUser[user.id] ?: 0
                val cash = state.cashBalances[user.id] ?: 0.0
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(user.displayName, fontWeight = FontWeight.Bold)
                            Text("كود ${user.userCode}", color = MaterialTheme.colorScheme.primary)
                        }
                        Text("المبيعات: %.2f • عدد الفواتير: %d".format(sales, count))
                        Text("المتبقي في الصندوق: %.2f".format(cash), color = Color.Gray)
                    }
                }
            }
            item { Text("ملخص العملاء", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            items(state.customers.sortedByDescending { kotlin.math.abs(it.balance) }.take(30), key = { it.id }) { customer ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(customer.name, fontWeight = FontWeight.Bold)
                            Text(customer.mobile.ifBlank { "بدون جوال" }, color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(accountBalance(customer.balance), color = balanceColor(customer.balance), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}